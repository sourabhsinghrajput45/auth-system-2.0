package com.auth.user.verification.repository;

import com.auth.user.verification.entity.EmailVerificationToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class EmailVerificationTokenRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void persist(EmailVerificationToken token) {
        entityManager.persist(token);
    }

    public Optional<EmailVerificationToken> findByToken(String token) {
        return entityManager
                .createQuery(
                        "SELECT t FROM EmailVerificationToken t WHERE t.token = :token",
                        EmailVerificationToken.class
                )
                .setParameter("token", token)
                .getResultStream()
                .findFirst();
    }

    @Transactional
    public void update(EmailVerificationToken token) {
        entityManager.merge(token);
    }
}
