package com.auth.security;

import io.quarkus.elytron.security.common.BcryptUtil;



public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
