package org.example.article.repositories;

import org.example.article.entities.EvalCycle;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.entities.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonographicRepository extends JpaRepository<Monographic, Long> {

    boolean existsByAuthorId(Long authorId);

    boolean existsByTitle(String title);

    @Query("""
          select distinct m from Monographic m
          left join fetch m.type
          left join fetch m.discipline
          left join fetch m.cycle
          left join fetch m.coauthors
          where m.id = :id
    """)
    Optional<Monographic> findWithAllRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"type", "discipline", "cycle", "coauthors"})
    Page<Monographic> findAll(Specification<Monographic> spec, Pageable pageable);

    List<Monographic> findAllByCycle(EvalCycle cycleId);

    List<Monographic> findAllByMeinMonoId(Long meinMonoId);
}
