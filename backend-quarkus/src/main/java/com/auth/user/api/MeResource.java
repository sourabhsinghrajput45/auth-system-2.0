package com.auth.user.api;

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
        // LOG: SecurityContext class (will be proxy)
        // -------------------------------------------------
        System.out.println("[ME] SecurityContext class = "
                + (securityContext != null
                ? securityContext.getClass().getName()
                : "null"));

        // -------------------------------------------------
        // AUTH CHECK (CORRECT WAY)
        // -------------------------------------------------
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            System.out.println("[ME] No authenticated principal → 401");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "authenticated", false
                    ))
                    .build();
        }

        String email = securityContext.getUserPrincipal().getName();
        System.out.println("[ME] Authenticated principal = " + email);

        return Response.ok(
                Map.of(
                        "authenticated", true,
                        "email", email
                )
        ).build();
    }
}
