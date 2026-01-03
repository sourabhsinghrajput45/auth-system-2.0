package com.auth.user.api;

import java.time.Duration;
import java.time.Instant;

import com.auth.user.entity.User;
import com.auth.user.service.UserService;
import com.auth.user.verification.entity.EmailVerificationToken;
import com.auth.user.verification.service.EmailVerificationService;
import com.auth.security.token.entity.AccessToken;
import com.auth.security.token.service.AccessTokenService;
import com.auth.security.PasswordHasher;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    ManagedExecutor executor;

    @Inject
    UserService userService;

    @Inject
    EmailVerificationService emailVerificationService;

    @Inject
    AccessTokenService accessTokenService;

    // =====================================================
    // SIGNUP
    // =====================================================
    @POST
    @Path("/signup")
    public Response signup(SignupRequest request) {

        if (request == null || request.email == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email and password are required"))
                    .build();
        }

        User user = userService.createUser(
                request.email,
                request.password
        );

        EmailVerificationToken token =
                emailVerificationService.createToken(user);

        // Send email async AFTER transaction commit
        executor.execute(() ->
                emailVerificationService.sendVerificationEmail(
                        user.getEmail(),
                        token.getToken()
                )
        );

        return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                        "message", "Signup successful. Please verify your email."
                ))
                .build();
    }

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================
    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token) {

        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Verification token is required")
                    .build();
        }

        emailVerificationService.verifyEmail(token);

        return Response.ok(
                "Your email is verified. You can access the portal"
        ).build();
    }

    // =====================================================
    // LOGIN (COOKIE-BASED)
    // =====================================================
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {

        if (request == null || request.email == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email and password are required"))
                    .build();
        }

        User user = userService
                .findByEmail(request.email)
                .orElseThrow(() ->
                        new WebApplicationException(
                                "Invalid email or password",
                                Response.Status.UNAUTHORIZED
                        )
                );

        boolean passwordMatches =
                PasswordHasher.matches(
                        request.password,
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new WebApplicationException(
                    "Invalid email or password",
                    Response.Status.UNAUTHORIZED
            );
        }

        // Issue access token
        AccessToken accessToken = accessTokenService.issueToken(user);

        // Set HTTP-only cookie
int maxAge = (int) Duration.between(
        Instant.now(),
        accessToken.getExpiresAt()
).getSeconds();

NewCookie cookie = new NewCookie.Builder("accessToken")
        .value(accessToken.getToken())
        .path("/")
        .secure(true)
        .httpOnly(true)
        .sameSite(NewCookie.SameSite.NONE)   //  REQUIRED
        .maxAge(maxAge)
        .build();

        // IMPORTANT:
        // Login is allowed even if email is NOT verified
        return Response.ok(
                Map.of(
                        "email", user.getEmail(),
                        "emailVerified", user.isEmailVerified()
                )
        ).cookie(cookie).build();
    }

    // =====================================================
    // AUTH STATUS (REACT USES THIS)
    // =====================================================
    @GET
    @Path("/status")
    public Response authStatus(@CookieParam("accessToken") String token) {

        if (token == null || token.isBlank()) {
            return Response.ok(
                    Map.of(
                            "authenticated", false,
                            "emailVerified", false
                    )
            ).build();
        }

        User user;

        try {
            user = accessTokenService.validate(token).getUser();
        } catch (Exception e) {
            return Response.ok(
                    Map.of(
                            "authenticated", false,
                            "emailVerified", false
                    )
            ).build();
        }

        return Response.ok(
                Map.of(
                        "authenticated", true,
                        "email", user.getEmail(),
                        "emailVerified", user.isEmailVerified()
                )
        ).build();
    }

    // =====================================================
    // LOGOUT
    // =====================================================
    @POST
    @Path("/logout")
    public Response logout() {

        // Expire cookie
NewCookie expiredCookie = new NewCookie.Builder("accessToken")
        .value("")
        .path("/")
        .secure(true)
        .httpOnly(true)
        .sameSite(NewCookie.SameSite.NONE)   // REQUIRED
        .maxAge(0)
        .build();


        return Response.ok(
                Map.of("message", "Logged out successfully")
        ).cookie(expiredCookie).build();
    }
}
