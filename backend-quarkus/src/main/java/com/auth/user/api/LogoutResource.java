package com.auth.user.api;

import com.auth.security.token.repository.AccessTokenRepository;
import com.auth.security.token.repository.RefreshTokenRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@Path("/auth/logout")
public class LogoutResource {

    @Inject
    AccessTokenRepository accessTokenRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @POST
    @Transactional
    public Response logout(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing access token")
                    .build();
        }

        String tokenValue = authHeader.substring("Bearer ".length());

        // Revoke access token
        accessTokenRepository
                .find("token = ?1 and revoked = false", tokenValue)
                .firstResultOptional()
                .ifPresent(token -> token.setRevoked(true));

        // Revoke ALL refresh tokens for this user (hard logout)
        refreshTokenRepository
                .find("""
                        user.id = (
                            select a.user.id from AccessToken a
                            where a.token = ?1
                        )
                        and revoked = false
                        """, tokenValue)
                .stream()
                .forEach(rt -> rt.setRevoked(true));

        return Response.ok("Logged out successfully").build();
    }
}
