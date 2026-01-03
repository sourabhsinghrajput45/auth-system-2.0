package com.auth.user.api;

import com.auth.security.context.UserSecurityContext;
import com.auth.user.entity.User;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Map;

@Path("/me")
public class MeResource {

    @GET
    public Response me(@Context SecurityContext securityContext) {

        // -------------------------------------------------
        // LOG: SecurityContext class
        // -------------------------------------------------
        System.out.println("[ME] SecurityContext class = "
                + (securityContext != null
                ? securityContext.getClass().getName()
                : "null"));

        if (!(securityContext instanceof UserSecurityContext)) {
            System.out.println("[ME] SecurityContext is NOT UserSecurityContext → 401");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "authenticated", false
                    ))
                    .build();
        }

        UserSecurityContext usc = (UserSecurityContext) securityContext;
        User user = usc.getUser();

        // -------------------------------------------------
        // LOG: User presence
        // -------------------------------------------------
        System.out.println("[ME] User from SecurityContext = "
                + (user != null ? user.getEmail() : "null"));

        if (user == null) {
            System.out.println("[ME] User is NULL → 401");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "authenticated", false
                    ))
                    .build();
        }

        System.out.println("[ME] Authenticated user confirmed");

        return Response.ok(
                Map.of(
                        "authenticated", true,
                        "email", user.getEmail(),
                        "emailVerified", user.isEmailVerified()
                )
        ).build();
    }
}
