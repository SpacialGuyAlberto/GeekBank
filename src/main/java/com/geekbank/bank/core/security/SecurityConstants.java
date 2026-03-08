package com.geekbank.bank.core.security;

public class SecurityConstants {
    public static final String JWT_COOKIE_NAME = "jwtToken";
    public static final String COOKIE_PATH = "/";
    public static final String SAME_SITE = "Lax";
    public static final long EXPIRATION_DAYS = 1;

    private SecurityConstants() {
        // Private constructor to hide the implicit public one
    }
}
