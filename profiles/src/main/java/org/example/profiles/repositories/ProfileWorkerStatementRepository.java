package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileWorkerStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProfileWorkerStatementRepository extends JpaRepository<ProfileWorkerStatement, Long> {
    Optional<ProfileWorkerStatement> findByIdUserIdAndIdDisciplineIdAndIdEvalYear(Long userId, Long disciplineId, Integer evalYear);

    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO profile_worker_statement
      (user_id, discipline_id, eval_year, fte, share_percent, slot_in_discipline, max_slots, max_mono_slots, created_at, updated_at)
    SELECT
      pwd.user_id,
      pwd.discipline_id,
      :year,
      1.0000,
      100.00,
      0.0000,
      1.0000,
      0.0000,
      NOW(),
      NOW()
    FROM profile_worker_discipline pwd
    ON CONFLICT (user_id, discipline_id, eval_year) DO NOTHING
""", nativeQuery = true)
    int initStatementsForYear(@Param("year") int year);


    @Modifying
    @Transactional
    @Query(value = """
  INSERT INTO profile_worker_statement
    (user_id, discipline_id, eval_year, fte, share_percent, slot_in_discipline, max_slots, max_mono_slots, created_at, updated_at)
  VALUES
    (:userId, :disciplineId, :year, 1.0000, 100.00, 0.0000, 1.0000, 0.0000, NOW(), NOW())
  ON CONFLICT (user_id, discipline_id, eval_year) DO NOTHING
""", nativeQuery = true)
    int initStatementForUserDisciplineYear(@Param("userId") long userId,
                                           @Param("disciplineId") long disciplineId,
                                           @Param("year") int year);
}
