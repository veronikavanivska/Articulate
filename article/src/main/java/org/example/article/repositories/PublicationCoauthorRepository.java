package org.example.article.repositories;

import io.grpc.Grpc;
import org.example.article.entities.PublicationCoauthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PublicationCoauthorRepository extends JpaRepository<PublicationCoauthor, Long> {
    List<PublicationCoauthor> findByPublicationIdOrderByPosition(Long publicationId);

    @Modifying
    @Transactional
    @Query(value = """
        update publication_coauthor
        set full_name = :fullName
        where user_id = :userId
        """, nativeQuery = true)
    int updateFullNameByUserId(@Param("userId") long userId, @Param("fullName") String fullName);

    @Modifying
    @Transactional
    @Query("DELETE FROM PublicationCoauthor c WHERE c.publication.id = :publicationId")
    void deleteByPublicationId(@Param("publicationId") Long publicationId);
}
