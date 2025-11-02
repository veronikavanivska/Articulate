package org.example.article.repositories;

import org.example.article.entities.MEiN.MeinVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MeinVersionRepository extends JpaRepository<MeinVersion, Long> {

    boolean existsById(Long id);

    Optional<MeinVersion> findByActiveTrue();

    @Modifying
    @Transactional
    @Query("""
      update MeinVersion v
      set v.active = false
      where v.active = true and v.id <> :id
    """)
    int deactivateAllExcept(@Param("id") long id);

    @Query("""
    SELECT me.active
    FROM MeinVersion me
    WHERE me.id = :id
    """)
    Optional<Boolean> isActive(@Param("id") long id);
}
