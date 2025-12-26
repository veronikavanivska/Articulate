package org.example.article.repositories;

import org.example.article.entities.EvalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvalCycleRepository extends JpaRepository<EvalCycle, Long> {
    @Query("SELECT c FROM EvalCycle c WHERE c.yearFrom <= :y AND c.yearTo >= :y")
    Optional<EvalCycle> findByYear(@Param("y") int year);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByName(String name);
    Optional<EvalCycle> findFirstByIsActiveTrue();


    @Query("""
        select (count(c) > 0)
        from EvalCycle c
        where (:from <= c.yearTo) and (:to >= c.yearFrom)
    """)
    boolean existsOverlapping(@Param("from") int yearFrom, @Param("to") int yearTo);

    @Query("""
      select (count(c) > 0)
      from EvalCycle c
      where c.id <> :id
        and :from <= c.yearTo
        and :to  >= c.yearFrom
    """)
    boolean existsOverlappingExcludeId(@Param("id") long id, @Param("from") int yearFrom, @Param("to") int yearTo);


    List<EvalCycle> findAllByMeinVersionId(Long versionId);

    @Modifying
    @Transactional
    @Query("update EvalCycle c set c.isActive = false where c.isActive = true")
    int deactivateAll();


    @Modifying
    @Transactional
    @Query("""
      update EvalCycle c
      set c.isActive = false
      where c.isActive = true and c.id <> :id
    """)
    int deactivateAllExcept(@Param("id") long id);

    @Query("""
    SELECT c FROM EvalCycle c
        WHERE c.meinMonoVersion.id = :versionId
    """)
    List<EvalCycle> findAllByMeinMonoVersion(@Param("versionId") long versionId);
}
