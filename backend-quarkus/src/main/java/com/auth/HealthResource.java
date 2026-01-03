package com.auth;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/health")
public class HealthResource {

    @GET
    public String health() {
        return "Backend is running";
    }
}
