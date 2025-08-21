package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileWorkerRepository extends JpaRepository<ProfileWorker, Long> {

    Optional<ProfileWorker> findByUserId(Long userId);
}
