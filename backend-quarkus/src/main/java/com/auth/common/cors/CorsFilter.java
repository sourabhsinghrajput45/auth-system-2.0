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

        // No Origin header → not a CORS request
        if (origin == null) {
            return;
        }

        // 🔒 Allow only known frontends
        if (
            origin.equals("http://localhost:5173") ||
            origin.equals("https://auth-system-2-0.vercel.app")
        ) {

            // IMPORTANT: use putSingle (not add)
            responseContext.getHeaders().putSingle(
                    "Access-Control-Allow-Origin", origin
            );

            responseContext.getHeaders().putSingle(
                    "Access-Control-Allow-Credentials", "true"
            );

            responseContext.getHeaders().putSingle(
                    "Access-Control-Allow-Headers",
                    "Content-Type, Authorization"
            );

            responseContext.getHeaders().putSingle(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS"
            );

            // Optional but recommended for proxies / CDNs
            responseContext.getHeaders().putSingle(
                    "Vary", "Origin"
            );
        }
    }
}
