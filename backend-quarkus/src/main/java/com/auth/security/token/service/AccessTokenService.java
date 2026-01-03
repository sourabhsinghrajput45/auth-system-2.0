package com.auth.security.token.service;

import com.auth.security.token.entity.AccessToken;
import com.auth.security.token.repository.AccessTokenRepository;
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
public class AccessTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    AccessTokenRepository tokenRepository;

    // =====================================================
    // ISSUE TOKEN (UNCHANGED BEHAVIOR)
    // =====================================================
    @Transactional
    public AccessToken issueToken(User user) {

        // Revoke existing active tokens for this user
        tokenRepository.find("user = ?1 and revoked = false", user)
                .list()
                .forEach(t -> t.setRevoked(true));

        Instant now = Instant.now();

        AccessToken token = new AccessToken();
        token.setUser(user);
        token.setToken(generateTokenValue());
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(15, ChronoUnit.MINUTES));
        token.setRevoked(false);

        tokenRepository.persist(token);
        return token;
    }

    // =====================================================
    // VALIDATE TOKEN (NEW – REQUIRED)
    // =====================================================
@Transactional
public AccessToken validate(String tokenValue) {

    if (tokenValue == null || tokenValue.isBlank()) {
        throw new SecurityException("Missing access token");
    }

    Optional<AccessToken> tokenOpt =
            tokenRepository.find("token = ?1", tokenValue)
                    .firstResultOptional();

    if (tokenOpt.isEmpty()) {
        throw new SecurityException("Invalid access token");
    }

    AccessToken token = tokenOpt.get();

    if (token.isRevoked()) {
        throw new SecurityException("Access token revoked");
    }

    if (token.getExpiresAt().isBefore(Instant.now())) {
        throw new SecurityException("Access token expired");
    }

    //  Force initialization while session is open
    token.getUser().getEmail();

    return token;
}

    // =====================================================
    // OPTIONAL: USER EXTRACTION (UTILITY)
    // =====================================================
    public User getUserFromToken(String tokenValue) {
        return validate(tokenValue).getUser();
    }

    // =====================================================
    // TOKEN GENERATION
    // =====================================================
    private String generateTokenValue() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
