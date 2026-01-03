package com.auth.user.api;

import com.auth.user.entity.User;
import com.auth.user.repository.UserRepository;
import com.auth.user.verification.repository.EmailVerificationTokenRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

@QuarkusTest
class AuthResourceTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    EmailVerificationTokenRepository tokenRepository;

    @Test
    void login_should_fail_if_email_not_found() {

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "missing@example.com",
                          "password": "password"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(409)
                .body(is("Invalid email or password"));
    }

    @Test
    void login_should_block_unverified_user() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(
                com.auth.security.PasswordHasher.hash("password")
        );
        user.setEmailVerified(false);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "test@example.com",
                          "password": "password"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body(is("You need to validate your email to access the portal"));
    }

    @Test
    void login_should_allow_verified_user() {

        User user = new User();
        user.setEmail("verified@example.com");
        user.setPasswordHash(
                com.auth.security.PasswordHasher.hash("password")
        );
        user.setEmailVerified(true);

        when(userRepository.findByEmail("verified@example.com"))
                .thenReturn(Optional.of(user));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "verified@example.com",
                          "password": "password"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body(is("Your email is validated. You can access the portal"));
    }
}
