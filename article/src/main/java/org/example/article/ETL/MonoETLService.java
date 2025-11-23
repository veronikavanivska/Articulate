package org.example.article.ETL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MonoETLService {


    private static final Pattern POZIOM_PATTERN =
            Pattern.compile("POZIOM\\s+([IVX]+).*?(\\d+)\\s+punkt", Pattern.CASE_INSENSITIVE);
    private static final Pattern FULL_ROW_PATTERN =
            Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(.+)$");
    private static final Pattern ID_ONLY_PATTERN =
            Pattern.compile("^(\\d+)\\s+(\\d+)$");

    private final JdbcTemplate jdbcTemplate;

    public MonoETLService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class PublisherRow {
        final Integer lp;
        final String uid;
        final String name;
        final Integer points;
        final String level;

        PublisherRow(Integer lp, String uid, String name, Integer points, String level) {
            this.lp = lp;
            this.uid = uid;
            this.name = name;
            this.points = points;
            this.level = level;
        }
    }


    @Transactional
    public Long importPDF(byte[] bytes, String fileName, String label, long importedBy) {
        String sha256 = DigestUtils.sha256Hex(bytes);

        Long versionId = tryInsertVersion(label,fileName,sha256,importedBy);
        if(versionId == null) return null;

        importMonoFromPdf(versionId,bytes);

        return versionId;
    }



    private void importMonoFromPdf(Long versionId, byte[] bytes) {
        final String INS_PUB = """
        INSERT INTO mein_mono_publisher(version_id, lp, uid, name, points, level)
        VALUES (?,?,?,?,?,?)
        """;

        List<PublisherRow> rows = new ArrayList<>();

        String currentLevel = null;
        Integer currentPoints = null;
        PublisherRow currentRow = null;
        String pendingNameForNextRow = null;

        boolean insideTable = false;

        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);

            String[] lines = text.split("\\R");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replace('\u00A0', ' ').trim();
                if (line.isEmpty()) continue;

                if (!insideTable) {
                    Matcher pm = POZIOM_PATTERN.matcher(line);
                    if (pm.find()) {
                        insideTable = true;
                        currentLevel = pm.group(1);
                        currentPoints = parseIntSafe(pm.group(2));
                    }
                    continue;
                }

                String lower = line.toLowerCase(Locale.ROOT);

                Matcher pm = POZIOM_PATTERN.matcher(line);
                if (pm.find()) {
                    if (currentRow != null) {
                        rows.add(currentRow);
                        currentRow = null;
                    }
                    currentLevel = pm.group(1);
                    currentPoints = parseIntSafe(pm.group(2));
                    pendingNameForNextRow = null;
                    continue;
                }

                if (isHeaderLine(lower)) {
                    continue;
                }

                String nextLine = (i + 1 < lines.length)
                        ? lines[i + 1].replace('\u00A0', ' ').trim()
                        : null;
                boolean nextIsIdOnly = false;
                if (nextLine != null && !nextLine.isEmpty()) {
                    nextIsIdOnly = ID_ONLY_PATTERN.matcher(nextLine).matches();
                }

                boolean startsWithDigit = Character.isDigit(line.charAt(0));

                if (startsWithDigit) {
                    Matcher full = FULL_ROW_PATTERN.matcher(line);
                    Matcher idOnly = ID_ONLY_PATTERN.matcher(line);

                    // lp uid nazwa...
                    if (full.matches()) {
                        if (currentRow != null) {
                            rows.add(currentRow);
                        }
                        Integer lp = parseIntSafe(full.group(1));
                        String uid = full.group(2);
                        String nameSeg = full.group(3).trim();

                        String fullName = nameSeg;
                        if (pendingNameForNextRow != null) {
                            fullName = (pendingNameForNextRow + " " + fullName).trim();
                            pendingNameForNextRow = null;
                        }

                        currentRow = new PublisherRow(lp, uid, fullName, currentPoints, currentLevel);
                        continue;
                    }


                    if (idOnly.matches()) {
                        if (currentRow != null) {
                            rows.add(currentRow);
                        }
                        Integer lp = parseIntSafe(idOnly.group(1));
                        String uid = idOnly.group(2);

                        String fullName = pendingNameForNextRow != null ? pendingNameForNextRow.trim() : "";
                        pendingNameForNextRow = null;

                        currentRow = new PublisherRow(lp, uid, fullName, currentPoints, currentLevel);
                        continue;
                    }


                    continue;
                }

                if (nextIsIdOnly) {
                    if (pendingNameForNextRow == null) {
                        pendingNameForNextRow = line;
                    } else {
                        pendingNameForNextRow = pendingNameForNextRow + " " + line;
                    }
                    continue;
                }

                if (currentRow != null) {
                    currentRow = new PublisherRow(
                            currentRow.lp,
                            currentRow.uid,
                            (currentRow.name + " " + line).trim(),
                            currentRow.points,
                            currentRow.level
                    );
                }
            }

            if (currentRow != null) {
                rows.add(currentRow);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading MEiN monograph PDF", e);
        }

        if (rows.isEmpty()) return;

        jdbcTemplate.batchUpdate(INS_PUB, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PublisherRow r = rows.get(i);

                int j = 1;
                ps.setLong(j++, versionId);

                if (r.lp == null) ps.setNull(j++, Types.INTEGER);
                else ps.setInt(j++, r.lp);

                ps.setString(j++, r.uid);
                ps.setString(j++, r.name);

                if (r.points == null) ps.setNull(j++, Types.INTEGER);
                else ps.setInt(j++, r.points);

                ps.setString(j++, r.level);
            }

            @Override
            public int getBatchSize() { return rows.size(); }
        });
    }

    private boolean isHeaderLine(String lower) {
        return lower.startsWith("lp.")
                || lower.startsWith("unikatowy")
                || lower.startsWith("identyfikator")
                || lower.startsWith("wydawnictwo")
                || lower.startsWith("wykaz wydawnictw");
    }

    private Integer parseIntSafe(String s) {
        try { return Integer.valueOf(s); }
        catch (NumberFormatException ex) { return null; }
    }

    private Long tryInsertVersion(String label, String fileName, String sha256, long importedBy) {
        int inserted = jdbcTemplate.update("""
        INSERT INTO mein_mono_version (label, source_filename, source_sha256, imported_by)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(source_sha256) DO NOTHING
        """, ps -> {
            ps.setString(1, label);
            ps.setString(2, fileName != null ? fileName : "upload.pdf");
            ps.setString(3, sha256);
            ps.setLong(4, importedBy);
        });

        if(inserted == 0) return null;

        return jdbcTemplate.query(
                "SELECT id FROM mein_mono_version WHERE source_sha256 = ?",
                    rs -> {
                        if(rs.next()) return rs.getLong(1);
                        throw new  IllegalStateException("Inserted version, but cannot read it back by hash");
                    }, sha256
        );
    }
}
