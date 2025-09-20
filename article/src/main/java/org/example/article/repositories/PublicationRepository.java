package org.example.article.repositories;

import org.example.article.entities.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    boolean existsByTitle(String title);
    boolean existsByAuthorId(Long id);

}
