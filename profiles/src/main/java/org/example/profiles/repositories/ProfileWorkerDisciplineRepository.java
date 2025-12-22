package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileWorkerDiscipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProfileWorkerDisciplineRepository extends JpaRepository<ProfileWorkerDiscipline, Long> {

    @Query("""
        select pwd from ProfileWorkerDiscipline pwd
        join fetch pwd.discipline d
        where pwd.id.userId = :userId
    """)
    List<ProfileWorkerDiscipline> findAllByUserIdWithDiscipline(@Param("userId") Long userId);


    boolean existsByIdUserIdAndIdDisciplineId(Long userId, Long disciplineId);

    @Modifying
    @Transactional
    void deleteByIdUserIdAndIdDisciplineId(Long userId, Long disciplineId);
}
