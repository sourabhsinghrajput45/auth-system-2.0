package com.auth.user.api;

import com.auth.security.token.entity.RefreshToken;
import com.auth.security.token.service.AccessTokenService;
import com.auth.security.token.service.RefreshTokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

@Path("/auth/refresh")
@Consumes("application/json")
@Produces("application/json")
public class TokenRefreshResource {

    @Inject
    RefreshTokenService refreshTokenService;

    @Inject
    AccessTokenService accessTokenService;

    public static class RefreshRequest {
        public String refreshToken;
    }

    @POST
    public Response refresh(RefreshRequest request) {

        if (request == null || request.refreshToken == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Refresh token is required")
                    .build();
        }

        var tokenOpt = refreshTokenService.findValid(request.refreshToken);

        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or revoked refresh token")
                    .build();
        }

        RefreshToken token = tokenOpt.get();

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Refresh token expired")
                    .build();
        }

        // ROTATION (this is the core security rule)
        token.setRevoked(true);

        var user = token.getUser();
        var newAccess = accessTokenService.issueToken(user);
        var newRefresh = refreshTokenService.issue(user);

        return Response.ok(
                Map.of(
                        "accessToken", newAccess.getToken(),
                        "accessTokenExpiresAt", newAccess.getExpiresAt(),
                        "refreshToken", newRefresh.getToken(),
                        "refreshTokenExpiresAt", newRefresh.getExpiresAt()
                )
        ).build();
    }
}
