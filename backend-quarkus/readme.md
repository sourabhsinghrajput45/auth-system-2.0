# Backend – Authentication & Session Management (Quarkus)

## Project Overview

This backend service implements a complete user authentication and session management system using **Quarkus** and **PostgreSQL**.

It fulfills all the requirements defined in the assignment, including secure password handling, email verification, login/logout, token-based session management, and backend testing using mocks.

The backend exposes REST APIs that can be consumed by a UI layer or a Node-based middleware.

---

## Tech Stack

- **Framework:** Quarkus
- **Language:** Java 21
- **Database:** PostgreSQL
- **ORM:** Hibernate ORM + Panache
- **Security:** BCrypt password hashing, Opaque Access & Refresh Tokens
- **Testing:** Quarkus Test + Mockito
- **Mailer:** Quarkus Mailer
- **Build Tool:** Maven

---

## Features Implemented

### 1. User Signup

Users can sign up using:
- Email
- Password

Passwords are never stored in plain text.

Passwords are stored using **salted BCrypt hashing**.

#### Security Implementation
- `BcryptUtil.bcryptHash()` is used for hashing
- Password verification uses `BcryptUtil.matches()`

---

### 2. Email Verification

- On signup, users receive a verification email
- Email contains a unique verification token
- User must verify email before accessing the portal

#### Verification Rules
- **Unverified users can log in**
    - After login, response message:
      ```
      "You need to validate your email to access the portal"
      ```
- **Verified users receive:**
    ```
    "Your email is validated. You can access the portal"
    ```

---

### 3. Login

- Users log in using email + password
- Password is verified against hashed value
- Login behavior depends on email verification status
- On successful login:
    - Access token is issued
    - Refresh token is issued

---

### 4. Session Management (Opaque Tokens)

This project uses **opaque tokens** instead of JWTs for session management.

#### Access Token
- Short-lived (15 minutes)
- Stored in database
- Used for authenticating API requests
- Revoked on logout or expiry

#### Refresh Token
- Long-lived
- Used to issue new access tokens
- Rotated on every refresh
- Old refresh tokens are revoked automatically

---

### 5. Token Rotation Logic

Token rotation is implemented during refresh:
- Old refresh token is marked `revoked = true`
- New access token is issued
- New refresh token is issued

Only **one active refresh token** exists per user session.

This prevents:
- Token replay attacks
- Session hijacking

---

### 6. Logout

Logout explicitly revokes:
- Current access token
- All active refresh tokens for the user

After logout:
- Accessing protected routes returns **401 Unauthorized**
- User must log in again

---

### 7. Protected Routes

- All non-auth routes require a valid access token
- Implemented using a `ContainerRequestFilter`

#### Token Validation Includes:
- Token existence
- Revocation check
- Expiry check

---

### 8. Persistence Layer

The following entities are persisted in PostgreSQL:
- User
- EmailVerificationToken
- AccessToken
- RefreshToken

Hibernate Panache is used for:
- Clean repository abstraction
- Readable database queries
- Transaction handling

---

### 9. Backend Testing (Mocking)

Backend APIs are tested using:
- `@QuarkusTest`
- `@InjectMock`
- Mockito

Repositories are mocked to test:
- Login failure cases
- Email verification checks
- Successful authentication flows

This ensures:
- No dependency on real database during tests
- Deterministic test results

---

## API Endpoints

### Authentication

| Method | Endpoint              | Description            |
|------|----------------------|------------------------|
| POST | `/auth/signup`       | Register new user      |
| GET  | `/auth/verify?token=`| Verify email           |
| POST | `/auth/login`        | Login user             |
| POST | `/auth/refresh`      | Refresh tokens         |
| POST | `/auth/logout`       | Logout user            |

### Protected Example

| Method | Endpoint | Description                    |
|------|----------|--------------------------------|
| GET  | `/me`    | Returns logged-in user email   |

---

## Sample Flow (End-to-End)

1. User signs up
2. Receives email verification link
3. Verifies email
4. Logs in
5. Receives access + refresh token
6. Accesses protected routes
7. Logs out
8. Session is invalidated

---

## How to Run

### Prerequisites
- Java 21
- PostgreSQL
- Maven

### Start Application

```bash
./mvnw quarkus:dev
```

### Backend Runs At
```
http://localhost:8080
```