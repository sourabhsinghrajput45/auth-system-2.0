package com.auth.security.token.service;

import com.auth.security.token.entity.RefreshToken;
import com.auth.security.token.repository.RefreshTokenRepository;
import com.auth.user.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    // =====================================================
    // ISSUE REFRESH TOKEN (INTERNAL USE ONLY)
    // =====================================================
    @Transactional
    public RefreshToken issue(User user) {

        // Revoke old refresh tokens for this user
        refreshTokenRepository
                .find("user = ?1 and revoked = false", user)
                .stream()
                .forEach(t -> t.setRevoked(true));

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(generate());
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        token.setRevoked(false);

        refreshTokenRepository.persist(token);
        return token;
    }

    // =====================================================
    // VALIDATE REFRESH TOKEN (BACKEND-ONLY)
    // =====================================================
    public Optional<RefreshToken> findValid(String tokenValue) {

        if (tokenValue == null || tokenValue.isBlank()) {
            return Optional.empty();
        }

        Optional<RefreshToken> tokenOpt =
                refreshTokenRepository
                        .find("token = ?1 and revoked = false", tokenValue)
                        .firstResultOptional();

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken token = tokenOpt.get();

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    // =====================================================
    // TOKEN GENERATION
    // =====================================================
    private String generate() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
