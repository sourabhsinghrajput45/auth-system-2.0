package com.auth.security.context;

import com.auth.user.entity.User;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class UserSecurityContext implements SecurityContext {

    private final User user;
    private final SecurityContext delegate;

    public UserSecurityContext(User user, SecurityContext delegate) {
        this.user = user;
        this.delegate = delegate;
    }

    // =====================================================
    // Principal (used by @Context SecurityContext)
    // =====================================================
    @Override
    public Principal getUserPrincipal() {
        if (user == null) {
            return null;
        }
        return () -> user.getEmail();
    }

    // =====================================================
    // Roles (not used yet, safe default)
    // =====================================================
    @Override
    public boolean isUserInRole(String role) {
        // Role-based auth not implemented
        return false;
    }

    // =====================================================
    // Transport security
    // =====================================================
    @Override
    public boolean isSecure() {
        return delegate != null && delegate.isSecure();
    }

    // =====================================================
    // Auth scheme (COOKIE-BASED, not Bearer)
    // =====================================================
    @Override
    public String getAuthenticationScheme() {
        return "COOKIE";
    }

    // =====================================================
    // Convenience accessor (optional, safe)
    // =====================================================
    public User getUser() {
        return user;
    }
}
