package com.auth.user.service;

import com.auth.user.entity.User;
import com.auth.user.repository.UserRepository;
import com.auth.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    // =====================================================
    // CREATE USER (SIGNUP)
    // =====================================================
    @Transactional
    public User createUser(String email, String plainPassword) {

        if (email == null || plainPassword == null) {
            throw new IllegalArgumentException("Email and password must be provided");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalStateException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(
                PasswordHasher.hash(plainPassword)
        );
        user.setEmailVerified(false);

        userRepository.persist(user);
        return user;
    }

    // =====================================================
    // LOOKUP
    // =====================================================
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================
    @Transactional
    public void markEmailAsVerified(User user) {
        if (user == null) {
            return;
        }
        user.setEmailVerified(true);
        userRepository.update(user);
    }
}
