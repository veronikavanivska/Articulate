package org.example.apigateway.config;


import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


@Configuration
public class SecurityConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.duration}")
    private Long duration;

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity httpSecurity,JwtDecoder jwtDecoder) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/registration" , "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/article/worker/**").hasRole("WORKER")
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        return httpSecurity.build();
    }


    @Bean
    public OAuth2TokenValidator<Jwt> versionValidator(StringRedisTemplate redis) {
        return jwt -> {
            String sub = jwt.getSubject();
            Number verNum = jwt.getClaim("ver");

            if (sub == null || verNum == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "missing subject or version", null));
            }

            String current = redis.opsForValue().get("usr:ver:" + sub);
            if (current == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "no version found in Redis", null));
            }

            long tokenVer = verNum.longValue();
            long redisVer = Long.parseLong(current);

            if (tokenVer != redisVer) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "stale token version", null));
            }

            return OAuth2TokenValidatorResult.success();
        };
    }
    @Bean
    public JwtDecoder jwtDecoder(OAuth2TokenValidator<Jwt> versionValidator) {

        SecretKey key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        OAuth2TokenValidator<Jwt> defaults = JwtValidators.createDefault();


        OAuth2TokenValidator<Jwt> subRequired = jwt ->
                (jwt.getSubject() != null && !jwt.getSubject().isBlank())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "missing subject", null));

        OAuth2TokenValidator<Jwt> composite = new DelegatingOAuth2TokenValidator<>(defaults, subRequired, versionValidator);
        decoder.setJwtValidator(composite);
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();
            return jwt.getSubject();
        }
        return null;
    }
}

