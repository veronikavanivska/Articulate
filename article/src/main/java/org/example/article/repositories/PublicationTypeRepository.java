package org.example.article.repositories;

import org.example.article.entities.PublicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicationTypeRepository extends JpaRepository<PublicationType, Long> {
    Optional<PublicationType> findByNameIgnoreCase(String name);
}
