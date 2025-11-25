package org.example.article.repositories;

import org.example.article.entities.PublicationCoauthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationCoauthorRepository extends JpaRepository<PublicationCoauthor, Long> {
    List<PublicationCoauthor> findByPublicationIdOrderByPosition(Long publicationId);
}
