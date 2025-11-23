package org.example.article.repositories;

import org.example.article.entities.MEiN.article.MeinJournalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MeinJournalCodeRepository extends JpaRepository<MeinJournalCode, Long> {
    @Query("""
        SELECT (COUNT(c) > 0)
        FROM MeinJournalCode c
        JOIN c.journal j
        JOIN j.version v
        WHERE v.active = TRUE
          AND j.id = :journalId
          AND EXISTS (
            SELECT 1
            FROM DisciplineMeinCode m
            WHERE m.id.meinCode = c.id.code
              AND m.id.disciplineId = :disciplineId
          )
    """)
    boolean existsActiveMatch(@Param("journalId") Long journalId,
                              @Param("disciplineId") Long disciplineId);

    @Query("""
        SELECT (COUNT(c) > 0)
        FROM MeinJournalCode c
        JOIN c.journal j
        WHERE j.id = :journalId
          AND c.id.versionId = :versionId
          AND EXISTS (
             SELECT 1
             FROM DisciplineMeinCode m
             WHERE m.id.meinCode = c.id.code
               AND m.id.disciplineId = :disciplineId
          )
    """)
    boolean existsMatchInVersion(@Param("journalId") Long journalId,
                                 @Param("disciplineId") Long disciplineId,
                                 @Param("versionId") Long versionId);


    @Query("select count(distinct mc.id.code) " +
            "from MeinJournalCode mc " +
            "where mc.id.versionId = :versionId")
    long countDistinctCodesInVersion(@Param("versionId") Long versionId);

    // Всі записи для конкретної версії і журналу
    @Query("""
        SELECT jc
        FROM MeinJournalCode jc
        JOIN FETCH jc.codeRef c
        WHERE jc.id.versionId = :versionId
          AND jc.id.journalId = :journalId
    """)
    List<MeinJournalCode> findAllByVersionIdAndJournalId(Long versionId, Long journalId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM mein_journal_code WHERE version_id = :vid", nativeQuery = true)
    int deleteCodesByVersion(@Param("vid") long vid);
}
