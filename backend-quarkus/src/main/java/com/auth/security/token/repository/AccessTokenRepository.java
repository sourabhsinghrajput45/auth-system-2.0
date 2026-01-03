package com.auth.security.token.repository;

import com.auth.security.token.entity.AccessToken;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class AccessTokenRepository implements PanacheRepository<AccessToken> {

    public Optional<AccessToken> findValidToken(String token) {
        return find(
                "token = ?1 and revoked = false and expiresAt > ?2",
                token,
                Instant.now()
        ).firstResultOptional();
    }
}
