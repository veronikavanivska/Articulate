package org.example.auth.repositories;


import org.example.auth.entities.RefreshTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {
    @Query(
            """
            select token from RefreshTokens token
                        where token.tokenHash = :hash and token.revoked = false and token.expiresAt > :now
            """
    )
    Optional<RefreshTokens> findActiveByHash(@Param("hash") String hash, @Param("now") Instant now);

    @Query("""
           update RefreshTokens rt
              set rt.revoked = true, rt.lastUsedAt = :now
            where rt.user.id = :userId and rt.revoked = false
           """)
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
