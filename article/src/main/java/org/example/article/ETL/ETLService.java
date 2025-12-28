package org.example.article.ETL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.article.helpers.DisciplineSyncService;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Service
public class ETLService {

    private final JdbcTemplate jdbcTemplate;
    private final DisciplineSyncService disciplineSyncService;
    public ETLService(JdbcTemplate jdbcTemplate, DisciplineSyncService disciplineSyncService) {
        this.disciplineSyncService = disciplineSyncService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long importExcel(byte[] bytes, String fileName, String label, long importedBy, boolean activateAfter) {

        String sha256  = DigestUtils.sha256Hex(bytes);

        Long versionId = tryInsertVersion(label, fileName, sha256, importedBy);
        if(versionId == null) return null;

        try(var in = new ByteArrayInputStream(bytes);
            var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(in)) {
            loadWorkbook(versionId, wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(activateAfter) activateVersion(versionId);

        return versionId;
    }

    private void activateVersion(Long versionId) {
        jdbcTemplate.update("UPDATE mein_version SET is_active = FALSE WHERE is_active");
        jdbcTemplate.update("UPDATE mein_version SET is_active = TRUE WHERE id = ?", versionId);
    }

    private void loadWorkbook(Long versionId, XSSFWorkbook wb) {
        Sheet sh = wb.getSheetAt(0);

        int headerRowIdx = ExcelHeader.findHeaderRow(sh, Set.of("Tytuł 1", "Tytul 1"));
        if (headerRowIdx < 0) {
            throw new IllegalStateException("Header row with 'Tytuł 1' not found");
        }

        Row topHeader = headerRowIdx > 0 ? sh.getRow(headerRowIdx - 1) : null;
        Row labelRow = sh.getRow(headerRowIdx);

        //name for codes
        Map<Integer, String> topNames = ExcelHeader.rowToMap(topHeader);
        //all labels
        Map<Integer, String> labelNames = ExcelHeader.rowToMap(labelRow);

        //Codes
        Map<Integer, String> codeColumns = ExcelHeader.detectCodeColumns(labelNames);

        updateCodesFromHeader(codeColumns,topNames);
        ensureDisciplinesFromCodes(codeColumns, topNames);


        Integer iLp      = ExcelHeader.find(labelNames, "Lp.", "Lp");
        Integer iUID     = ExcelHeader.find(labelNames, "Unikatowy Identyfikator Czasopisma");
        Integer iTytul1  = ExcelHeader.find(labelNames, "Tytuł 1", "Tytul 1");
        Integer iISSN1   = ExcelHeader.find(labelNames, "issn", "ISSN");
        Integer iEISSN1  = ExcelHeader.find(labelNames, "e-issn", "E-ISSN", "eISSN");
        Integer iTytul2  = ExcelHeader.find(labelNames, "Tytuł 2", "Tytul 2");
        Integer iISSN2   = ExcelHeader.nextAfter(labelNames, iTytul2, "issn", "ISSN");
        Integer iEISSN2  = ExcelHeader.nextAfter(labelNames, iTytul2, "e-issn", "E-ISSN", "eISSN");
        Integer iPoints  = ExcelHeader.find(labelNames, "Punktacja", "Punkty");

        final String INS_JOURNAL = """
            INSERT INTO mein_journal(version_id, lp, uid, title_1, issn, eissn, title_2, issn2, eissn2, points)
            VALUES (?,?,?,?,?,?,?,?,?,?)
        """;

        List<Long> journalIds = new ArrayList<>();
        List<Map.Entry<Integer, String>> codeColumnsList = new ArrayList<>(codeColumns.entrySet());

        List<boolean[]> rowsChecked = new ArrayList<>();

        for(int i = headerRowIdx + 1; i<=sh.getLastRowNum();i++){
            Row row = sh.getRow(i);
            if(row == null) continue;

            Integer lp = ExcelHeader.toInt(ExcelHeader.getCell(row, iLp));
            String uid = ExcelHeader.getCell(row, iUID);
            String t1  = ExcelHeader.getCell(row, iTytul1);
            String issn1  = IssnUtil.normalize(ExcelHeader.getCell(row, iISSN1));
            String eissn1 = IssnUtil.normalize(ExcelHeader.getCell(row, iEISSN1));
            String t2     = ExcelHeader.getCell(row, iTytul2);
            String issn2  = IssnUtil.normalize(ExcelHeader.getCell(row, iISSN2));
            String eissn2 = IssnUtil.normalize(ExcelHeader.getCell(row, iEISSN2));
            Integer points = ExcelHeader.toInt(ExcelHeader.getCell(row, iPoints));
            if (points == null) points = 0;

            KeyHolder kh = new GeneratedKeyHolder();

            final Integer finalPoints = points;
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(INS_JOURNAL, new String[]{"id"});

                int j = 1;
                ps.setLong(j++,  versionId);
                if (lp == null) ps.setNull(j++, java.sql.Types.INTEGER); else ps.setInt(j++, lp);
                ps.setString(j++, uid);
                ps.setString(j++, t1);
                ps.setString(j++, issn1);
                ps.setString(j++, eissn1);
                ps.setString(j++, t2);
                ps.setString(j++, issn2);
                ps.setString(j++, eissn2);
                ps.setInt(j++, finalPoints);
                return ps;

            }, kh);

            Long jId = Objects.requireNonNull(kh.getKey()).longValue();
            journalIds.add(jId);

            boolean[] checked = new boolean[codeColumnsList.size()];
            for(int k = 0;k<codeColumnsList.size();k++){
                int colId = codeColumnsList.get(k).getKey();
                String cell = ExcelHeader.getCell(row, colId);
                checked[k] = ExcelHeader.isChecked(cell);

            }
            rowsChecked.add(checked);
        }

        final String INS_JC = "INSERT INTO mein_journal_code(version_id, journal_id, code) VALUES (?,?,?) ON CONFLICT DO NOTHING";

        final List<long[]> triples = new ArrayList<>();
        for(int rowId = 0; rowId<journalIds.size(); rowId++){
            long jId = journalIds.get(rowId);
            boolean[] checked = rowsChecked.get(rowId);
            for(int i = 0; i< checked.length;i++){
                if(checked[i]){
                    triples.add(new long[]{versionId,jId, i});
                }
            }
        }

        jdbcTemplate.batchUpdate(INS_JC, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long[] triple = triples.get(i);
                long versionId = triple[0];
                long journalId = triple[1];
                int codeId = (int) triple[2];
                String code = codeColumnsList.get(codeId).getValue();

                ps.setLong(1, versionId);
                ps.setLong(2, journalId);
                ps.setString(3, code);
            }
            @Override
            public int getBatchSize() { return triples.size(); }
        });
    }

    private void updateCodesFromHeader(Map<Integer, String> codeColumns, Map<Integer, String> topNames) {
        final String UPSERT = """
                INSERT INTO mein_code(code, name) VALUES (?, ?)
                  ON CONFLICT (code) DO UPDATE SET name = COALESCE(EXCLUDED.name, mein_code.name)
                """;

        jdbcTemplate.batchUpdate(UPSERT, new BatchPreparedStatementSetter() {
           final List<Map.Entry<Integer,String>> list = new ArrayList<>(codeColumns.entrySet());
           @Override
           public void setValues(PreparedStatement ps, int i) throws SQLException {
               Map.Entry<Integer,String> entry = list.get(i);
               int colId = entry.getKey();
               String code = entry.getValue();
               String name = topNames.getOrDefault(colId,null);
               if (name != null && "nan".equalsIgnoreCase(name)) name = null;
               ps.setString(1, code);
               ps.setString(2, name);
           }
           @Override
           public int getBatchSize() {return list.size();}
        });
    }


    private Long tryInsertVersion(String label, String filename, String sha256, long importedBy ){

            int inserted = jdbcTemplate.update("""
                INSERT INTO mein_version (label, source_filename, source_sha256, imported_by)
                VALUES(?,?,?,?)
                ON CONFLICT(source_sha256) DO NOTHING
            """, ps ->{
                ps.setString(1, label);
                ps.setString(2, filename != null ? filename : "upload.xlsx");
                ps.setString(3, sha256);
                ps.setLong(4, importedBy);
            });

            if(inserted == 0) return null;

            return jdbcTemplate.query(
                    " SELECT id FROM mein_version WHERE source_sha256 = ?",
                    rs->{
                        if(rs.next()) return rs.getLong(1);
                        throw new IllegalStateException("Inserted version, but cannot read it back by hash");
                    }, sha256
            );
    }

    private void ensureDisciplinesFromCodes(Map<Integer, String> codeColumns, Map<Integer, String> topNames) {

        final String SQL_FIND_DISC_ID_BY_CODE =
                "SELECT discipline_id FROM discipline_mein_code WHERE mein_code = ? LIMIT 1";

        // wymaga UNIQUE(name) w article.discipline
        final String SQL_UPSERT_DISC_BY_NAME_RETURN_ID = """
        INSERT INTO discipline(name)
        VALUES (?)
        ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id
    """;

        final String SQL_UPDATE_DISC_NAME =
                "UPDATE discipline SET name = ? WHERE id = ?";

        final String SQL_LINK = """
        INSERT INTO discipline_mein_code(discipline_id, mein_code)
        VALUES (?, ?)
        ON CONFLICT DO NOTHING
    """;

        // żeby nie robić sync kilka razy dla tej samej dyscypliny w jednej paczce
        Map<Long, String> touched = new HashMap<>();

        // Iterujemy po kolumnach-kodach
        for (var entry : codeColumns.entrySet()) {
            int colId = entry.getKey();
            String code = entry.getValue();

            String name = topNames.get(colId);
            if (name == null) continue;

            name = name.trim();
            if (name.isBlank() || "nan".equalsIgnoreCase(name)) continue;

            Long disciplineId = null;

            // 1) jeśli code już ma mapping -> użyj go (i ewentualnie zrób rename)
            disciplineId = jdbcTemplate.query(SQL_FIND_DISC_ID_BY_CODE, ps -> ps.setString(1, code), rs ->
                    rs.next() ? rs.getLong(1) : null
            );

            if (disciplineId != null) {
                // Proste podejście: aktualizuj nazwę zawsze (tani update)
                jdbcTemplate.update(SQL_UPDATE_DISC_NAME, name, disciplineId);
            } else {
                // 2) ensure discipline po nazwie i weź id
                disciplineId = jdbcTemplate.queryForObject(SQL_UPSERT_DISC_BY_NAME_RETURN_ID, Long.class, name);

                // 3) link discipline <-> code
                jdbcTemplate.update(SQL_LINK, disciplineId, code);
            }

            touched.put(disciplineId, name);
        }

        disciplineSyncService.syncUpserts(touched);
    }

}
