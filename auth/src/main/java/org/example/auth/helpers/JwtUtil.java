package org.example.auth.helpers;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.auth.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.duration}")
    private long duration;


    public String generateToken(Long userId, List<String> roles, int tokenVersion) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .expiresAt(Instant.now().plus(duration, ChronoUnit.SECONDS))
                .issuedAt(Instant.now())
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("ver",tokenVersion)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS512).build();

        SecretKey key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(key);

        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);

        Jwt jwt = encoder.encode(JwtEncoderParameters.from(jwsHeader, claims));

        return jwt.getTokenValue();
    }
}
