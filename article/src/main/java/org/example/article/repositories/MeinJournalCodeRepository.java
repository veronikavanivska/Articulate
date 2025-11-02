package org.example.article.repositories;

import org.example.article.entities.MEiN.MeinJournalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeinJournalCodeRepository extends JpaRepository<MeinJournalCode, Long> {
    @Query("""
          SELECT (COUNT(c) > 0)
          FROM MeinJournalCode c
          JOIN c.version v
          WHERE v.active = TRUE
            AND c.journal.id = :journalId
            AND EXISTS (
              SELECT 1
              FROM DisciplineMeinCode m
              WHERE m.id.meinCode = c.id.code
                AND m.id.disciplineId = :disciplineId
            )
""")
    boolean existsActiveMatch(@Param("journalId") Long journalId,
                              @Param("disciplineId") Long disciplineId);


    @Query("select count(distinct mc.id.code) " +
            "from MeinJournalCode mc " +
            "where mc.id.versionId = :versionId")
    long countDistinctCodesInVersion(@Param("versionId") Long versionId);

    @Query("""
        SELECT jc
        FROM MeinJournalCode jc
        JOIN FETCH jc.codeRef c
        WHERE jc.version.id = :versionId
          AND jc.journal.id = :journalId
    """)
    List<MeinJournalCode> findAllByVersionIdAndJournalId(Long versionId, Long journalId);
}
