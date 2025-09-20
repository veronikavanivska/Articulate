package org.example.article.repositories;

import org.example.article.entities.EvalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvalCycleRepository extends JpaRepository<EvalCycle, Long> {
    @Query("SELECT c FROM EvalCycle c WHERE c.yearFrom <= :y AND c.yearTo >= :y")
    Optional<EvalCycle> findByYear(@Param("y") int year);
}
