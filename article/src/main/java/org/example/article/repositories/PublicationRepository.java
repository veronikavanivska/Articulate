package org.example.article.repositories;

import org.example.article.entities.EvalCycle;
import org.example.article.entities.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long>, JpaSpecificationExecutor<Publication> {

    boolean existsByTitle(String title);
    boolean existsByAuthorId(Long id);
    @Query("""
          select distinct p from Publication p
          left join fetch p.type
          left join fetch p.discipline
          left join fetch p.cycle
          left join fetch p.coauthors
          where p.id = :id
    """)
    Optional<Publication> findWithAllRelations(@Param("id") Long id);


    @EntityGraph(attributePaths = {"type", "discipline", "cycle", "coauthors"})
    Page<Publication> findAll(Specification<Publication> spec, Pageable pageable);

    List<Publication> findAllByCycle(EvalCycle cycleId);
    List<Publication> findAllByMeinVersionId(Long meinVersionId);
}
