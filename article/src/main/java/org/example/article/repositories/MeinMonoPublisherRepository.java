package org.example.article.repositories;

import org.example.article.entities.MEiN.article.MeinJournal;
import org.example.article.entities.MEiN.monographs.MeinMonoPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeinMonoPublisherRepository extends JpaRepository<MeinMonoPublisher, Long> {

    @Query("""
    SELECT p FROM MeinMonoPublisher p
    WHERE p.version.id = :versionId
    AND p.name = :name
    ORDER BY p.id DESC
    """)
    Optional<MeinMonoPublisher> findByVersionIdAndTitle(@Param("versionId") Long versionId,@Param("name") String title);

    Page<MeinMonoPublisher> findByVersion_Id(Long versionId, Pageable pageable);

    @Query("""
    SELECT (COUNT(p) > 0)
    FROM MeinMonoPublisher p
    WHERE p.version.id = :versionId and p.id = :publisherId
    """)
    boolean existsMatchInVersion(@Param("versionId") Long versionId, @Param("publisherId") Long publisherId);

    @Query(value = """
              SELECT COUNT(DISTINCT COALESCE(NULLIF(uid,'')))
              FROM mein_mono_publisher
              WHERE version_id = :versionId
              """, nativeQuery = true)
    long countPublishers(@Param("versionId") long versionId);

}
