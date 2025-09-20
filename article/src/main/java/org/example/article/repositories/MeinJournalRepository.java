package org.example.article.repositories;

import org.example.article.entities.MEiN.MeinJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeinJournalRepository extends JpaRepository<MeinJournal, Long> {

    @Query("""
        SELECT j FROM MeinJournal j
            WHERE j.version.active = true
                AND (
                        (j.issn  = :issn OR j.eissn  = :eissn)
                        OR (j.issn2 = :issn OR j.eissn2 = :eissn)
                    )
                    ORDER BY j.id DESC           
    """)
    Optional<MeinJournal> findActiveByIssnOrEissn(@Param("issn") String issn,
                                                  @Param("eissn") String eissn);
}
