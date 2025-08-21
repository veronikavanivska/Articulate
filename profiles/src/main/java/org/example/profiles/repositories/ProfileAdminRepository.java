package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileAdminRepository extends JpaRepository<ProfileAdmin, Long> {

    Optional<ProfileAdmin> findByUserId(Long userId);
}
