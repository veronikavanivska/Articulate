package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileUserRepository extends JpaRepository<ProfileUser, Long> {

    Optional<ProfileUser> findByUserId(Long userId);
}
