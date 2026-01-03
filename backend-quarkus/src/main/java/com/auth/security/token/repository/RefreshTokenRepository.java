package com.auth.security.token.repository;

import com.auth.security.token.entity.RefreshToken;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepository<RefreshToken> {
}
