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

        String path = requestContext.getUriInfo().getRequestUri().getPath();
        System.out.println("[FILTER] incoming request path = " + path);
        System.out.println("[FILTER] method = " + requestContext.getMethod());

        // -------------------------------------------------
        // Allow CORS preflight requests
        // -------------------------------------------------
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            System.out.println("[FILTER] OPTIONS request → bypass");
            return;
        }

        // -------------------------------------------------
        // Allow unauthenticated endpoints
        // -------------------------------------------------
        if (path.startsWith("auth") || path.startsWith("/auth")) {
            System.out.println("[FILTER] auth endpoint → bypass");
            return;
        }

        // -------------------------------------------------
        // Read access token from HTTP-only cookie
        // -------------------------------------------------
        Cookie cookie = requestContext.getCookies().get("accessToken");
        System.out.println("[FILTER] cookie present = " + (cookie != null));

        if (cookie == null) {
            System.out.println("[FILTER] cookie is NULL → 401");
            abort(requestContext);
            return;
        }

        System.out.println("[FILTER] cookie value length = "
                + (cookie.getValue() != null ? cookie.getValue().length() : "null"));

        AccessToken token;
        try {
            token = accessTokenService.validate(cookie.getValue());
            System.out.println("[FILTER] token validated successfully");
        } catch (Exception e) {
            System.out.println("[FILTER] token validation FAILED");
            e.printStackTrace();
            abort(requestContext);
            return;
        }

        User user = token.getUser();
        System.out.println("[FILTER] user from token = "
                + (user != null ? user.getEmail() : "null"));

        // -------------------------------------------------
        // Attach authenticated user to security context
        // -------------------------------------------------
        requestContext.setSecurityContext(
                new UserSecurityContext(
                        user,
                        requestContext.getSecurityContext()
                )
        );

        System.out.println("[FILTER] UserSecurityContext ATTACHED");
    }

    private void abort(ContainerRequestContext ctx) {
        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"authenticated\":false}")
                        .build()
        );
    }
}
