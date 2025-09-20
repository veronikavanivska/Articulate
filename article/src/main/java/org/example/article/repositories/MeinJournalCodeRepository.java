package org.example.article.repositories;

import org.example.article.entities.MEiN.MeinJournalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
