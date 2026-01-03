package com.auth.user.api;

import java.time.Instant;

public class LoginResponse {

    public String accessToken;
    public Instant expiresAt;

    public LoginResponse(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }
}
