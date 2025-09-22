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
                         AND (j.title1  = :title OR j.title2  = :title)
                    ORDER BY j.id DESC           
    """)
    Optional<MeinJournal> findActiveByIssnOrEissnAndTitle(@Param("issn") String issn,
                                                  @Param("eissn") String eissn,@Param("title") String title);


    @Query("""
            select (count(m) > 0) 
                from MeinJournal m 
                   WHERE m.version.active = true
                       AND(
                            (:issn is null OR m.issn  = :issn OR m.issn2 = :issn)
                         AND (:eissn is null OR m.eissn  = :eissn OR m.eissn2 = :eissn)
                                )
    """)
   boolean existsByIssnAndEissn(@Param("issn") String issn,
                                @Param("eissn") String eissn);
}
