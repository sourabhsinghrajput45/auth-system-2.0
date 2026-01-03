package com.auth.user.repository;

import com.auth.user.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void persist(User user) {
        entityManager.persist(user);
    }

    public Optional<User> findByEmail(String email) {
        return entityManager
                .createQuery(
                        "SELECT u FROM User u WHERE u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Transactional
    public void update(User user) {
        entityManager.merge(user);
    }
}
