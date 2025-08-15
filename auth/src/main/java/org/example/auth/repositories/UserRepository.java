package org.example.auth.repositories;

import org.example.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    @Query("""
        select u from User u
        left join fetch u.roles
        where u.email = :email
    """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    Optional<User> findUsersById(Long id);

   // Long findIdByEmail(String email);
}
