package org.example.article.repositories;

import org.example.article.entities.EvalCycle;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.Monographic;
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
public interface MonographChapterRepository extends JpaRepository<MonographChapter, Long> {
    boolean existsByAuthorId(Long authorId);

    @Query("""
          select distinct m from MonographChapter m
          left join fetch m.type
          left join fetch m.discipline
          left join fetch m.cycle
          left join fetch m.coauthors
          where m.id = :id
    """)
    Optional<MonographChapter> findWithAllRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"type", "discipline", "cycle", "coauthors"})
    Page<MonographChapter> findAll(Specification<MonographChapter> spec, Pageable pageable);

    List<MonographChapter> findAllByCycle(EvalCycle cycle);

    List<MonographChapter> findAllByMeinMonoId(Long meinMonoId);
}
