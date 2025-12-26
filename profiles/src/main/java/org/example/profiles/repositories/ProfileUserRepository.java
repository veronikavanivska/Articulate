package org.example.profiles.repositories;

import org.example.profiles.entities.ProfileUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileUserRepository extends JpaRepository<ProfileUser, Long> {

    Optional<ProfileUser> findByUserId(Long userId);
    @EntityGraph(attributePaths = {"worker", "admin"})
    @Query("""
        select pu
        from ProfileUser pu
        where (:q = '' or pu.fullname is not null and lower(pu.fullname) like lower(concat('%', :q, '%')))
    """)
    Page<ProfileUser> searchByFullname(@Param("q") String q, Pageable pageable);

}
