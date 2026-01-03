package com.auth.common.cors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String origin = requestContext.getHeaderString("Origin");

        if (origin == null) return;

        if (
                origin.equals("http://localhost:5173") ||
                        origin.equals("https://YOUR_VERCEL_APP.vercel.app")
        ) {
            responseContext.getHeaders().add(
                    "Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add(
                    "Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add(
                    "Access-Control-Allow-Headers", "Content-Type");
            responseContext.getHeaders().add(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
        }
    }
}
