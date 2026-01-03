package com.auth.security.token.filter;

import com.auth.security.context.UserSecurityContext;
import com.auth.security.token.entity.AccessToken;
import com.auth.security.token.service.AccessTokenService;
import com.auth.user.entity.User;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ActivateRequestContext
public class AccessTokenFilter implements ContainerRequestFilter {

    @Inject
    AccessTokenService accessTokenService;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String path = requestContext.getUriInfo().getPath();

        // -------------------------------------------------
        // Allow CORS preflight requests
        // -------------------------------------------------
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        // -------------------------------------------------
        // Allow unauthenticated endpoints
        // -------------------------------------------------
        if (
                path.startsWith("auth")
                || path.startsWith("/auth")
        ) {
            return;
        }

        // -------------------------------------------------
        // Read access token from HTTP-only cookie
        // -------------------------------------------------
        Cookie cookie = requestContext
                .getCookies()
                .get("accessToken");

        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            abort(requestContext);
            return;
        }

        AccessToken token;
        try {
            token = accessTokenService.validate(cookie.getValue());
        } catch (Exception e) {
            abort(requestContext);
            return;
        }

        User user = token.getUser();

        // -------------------------------------------------
        // Attach authenticated user to security context
        // -------------------------------------------------
        requestContext.setSecurityContext(
                new UserSecurityContext(
                        user,
                        requestContext.getSecurityContext()
                )
        );
    }

    private void abort(ContainerRequestContext ctx) {
        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"authenticated\":false}")
                        .build()
        );
    }
}
