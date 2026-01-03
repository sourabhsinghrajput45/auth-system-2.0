package com.auth.security.token.job;

import com.auth.security.token.repository.AccessTokenRepository;
import com.auth.security.token.repository.RefreshTokenRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class TokenCleanupJob {

    @Inject
    AccessTokenRepository accessTokenRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Scheduled(every = "24h")
    @Transactional
    void cleanup() {

        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        accessTokenRepository
                .delete("revoked = true and expiresAt < ?1", cutoff);

        refreshTokenRepository
                .delete("revoked = true and expiresAt < ?1", cutoff);
    }
}
