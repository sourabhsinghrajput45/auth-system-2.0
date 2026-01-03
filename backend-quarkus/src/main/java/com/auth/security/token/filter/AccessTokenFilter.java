package com.auth.security.token.filter;

import com.auth.security.context.UserSecurityContext;
import com.auth.security.token.entity.AccessToken;
import com.auth.security.token.service.AccessTokenService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ActivateRequestContext
public class AccessTokenFilter implements ContainerRequestFilter {

    @Inject
    AccessTokenService accessTokenService;

    /**
     * Explicit whitelist of public endpoints.
     * This avoids fragile string prefix logic and is PROD-safe.
     */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/logout",
            "/auth/signup",
            "/auth/status",
            "/auth/verify"
    );

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        System.out.println("[FILTER] path = " + path + ", method = " + method);

        // -------------------------------------------------
        // Allow CORS preflight requests
        // -------------------------------------------------
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("[FILTER] OPTIONS → bypass");
            return;
        }

        // -------------------------------------------------
        // Allow explicitly whitelisted public endpoints
        // -------------------------------------------------
        if (PUBLIC_PATHS.contains(path)) {
            System.out.println("[FILTER] public endpoint → bypass");
            return;
        }

        // -------------------------------------------------
        // Read access token from HTTP-only cookie
        // -------------------------------------------------
        Cookie cookie = requestContext.getCookies().get("accessToken");

        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            System.out.println("[FILTER] missing access token → 401");
            abort(requestContext);
            return;
        }

        AccessToken token;
        try {
            token = accessTokenService.validate(cookie.getValue());
        } catch (Exception e) {
            System.out.println("[FILTER] invalid / expired token → 401");
            abort(requestContext);
            return;
        }

        // -------------------------------------------------
        // Attach authenticated user to security context
        // -------------------------------------------------
        requestContext.setSecurityContext(
                new UserSecurityContext(
                        token.getUser(),
                        requestContext.getSecurityContext()
                )
        );

        System.out.println("[FILTER] authentication successful");
    }

    private void abort(ContainerRequestContext ctx) {
        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"authenticated\":false}")
                        .build()
        );
    }
}
