package org.example.article.repositories;

import org.example.article.entities.MEiN.article.MeinJournal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
       SELECT j
       FROM MeinJournal j
       WHERE j.version.id = :versionId
          AND (
                        (j.issn  = :issn OR j.eissn  = :eissn)
                        OR (j.issn2 = :issn OR j.eissn2 = :eissn)
                    ) 
                         AND (j.title1  = :title OR j.title2  = :title)
                    ORDER BY j.id DESC         
    """)
    Optional<MeinJournal> findByVersionAndIssnOrEissnAndTitle(@Param("versionId") Long versionId,
                                                              @Param("issn") String issn,
                                                              @Param("eissn") String eissn,
                                                              @Param("title") String title);
    @Query("""
            select (count(m) > 0) 
                from MeinJournal m 
                   WHERE m.version.id = :versionId
                       AND (
                            (:issn is null OR m.issn  = :issn OR m.issn2 = :issn)
                         OR (:eissn is null OR m.eissn  = :eissn OR m.eissn2 = :eissn)
                                )
    """)
    boolean existsByIssnAndEissn(@Param("versionId")  long versionId,@Param("issn") String issn,
                                @Param("eissn") String eissn);


    @Query(value = """
              SELECT COUNT(DISTINCT COALESCE(NULLIF(uid,''), issn, eissn, issn2, eissn2))
              FROM mein_journal
              WHERE version_id = :versionId
              """, nativeQuery = true)
    long countJournals(@Param("versionId") long versionId);

    @Query("""
  SELECT COUNT(DISTINCT m.uid)
  FROM MeinJournal m
  WHERE m.version.id = :versionId
    AND m.uid IS NOT NULL
""")
    long countDistinctUid(@Param("versionId") long versionId);

    Optional<MeinJournal> findByIdAndVersion_Id(Long id, Long versionId);
    Page<MeinJournal> findByVersion_Id(Long versionId, Pageable pageable);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM mein_journal WHERE version_id = :vid", nativeQuery = true)
    int deleteJournalsByVersion(@Param("vid") long vid);
}
