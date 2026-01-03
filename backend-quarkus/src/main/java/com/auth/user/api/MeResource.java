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

        if (!(securityContext instanceof UserSecurityContext)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "authenticated", false
                    ))
                    .build();
        }

        UserSecurityContext usc = (UserSecurityContext) securityContext;
        User user = usc.getUser();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "authenticated", false
                    ))
                    .build();
        }

        return Response.ok(
                Map.of(
                        "authenticated", true,
                        "email", user.getEmail(),
                        "emailVerified", user.isEmailVerified()
                )
        ).build();
    }
}
