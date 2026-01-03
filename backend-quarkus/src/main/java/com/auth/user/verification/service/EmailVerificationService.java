package com.auth.user.verification.service;

import com.auth.user.entity.User;
import com.auth.user.service.UserService;
import com.auth.user.verification.entity.EmailVerificationToken;
import com.auth.user.verification.repository.EmailVerificationTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ApplicationScoped
public class EmailVerificationService {

    private static final long TOKEN_VALIDITY_MINUTES = 30;

    @Inject
    EmailVerificationTokenRepository tokenRepository;

    @Inject
    UserService userService;

    @Inject
    com.auth.common.mail.MailService mailService;

    @ConfigProperty(name = "app.base-url")
    String baseUrl;

    /**
     *      TRANSACTIONAL ONLY
     * Creates and persists the verification token.
     *  NO async,  NO email sending here.
     */
    @Transactional
    public EmailVerificationToken createToken(User user) {

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(
                Instant.now().plus(TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES)
        );
        token.setUsed(false);

        tokenRepository.persist(token);

        return token;
    }

    /**
     *TRANSACTIONAL
     * Verifies email and updates user + token state.
     */
    @Transactional
    public void verifyEmail(String tokenValue) {

        EmailVerificationToken token = tokenRepository
                .findByToken(tokenValue)
                .orElseThrow(() ->
                        new IllegalStateException("Invalid verification token")
                );

        if (token.isUsed()) {
            throw new IllegalStateException("Verification token already used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Verification token has expired");
        }

        User user = token.getUser();
        userService.markEmailAsVerified(user);

        token.setUsed(true);
        tokenRepository.update(token);
    }

    /**
     *  NON-TRANSACTIONAL
     * Sends verification email.
     * Safe to call asynchronously AFTER DB commit.
     */
    public void sendVerificationEmail(String email, String token) {

        String verificationLink =
                baseUrl + "/auth/verify?token=" + token;

        mailService.sendEmail(
                "ghostirl200@gmail.com", // Forcing my mail due to restrictions
                "Verify your email",
                "Click the link to verify your email:\n" + verificationLink
        );
    }

    /**
     * Helper method.
     */
    public boolean isEmailVerified(User user) {
        return user.isEmailVerified();
    }
}
