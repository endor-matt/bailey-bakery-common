package com.baileybakery.common.crypto;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cryptographically secure token generation. Used for API keys
 * and session identifiers where predictability is unacceptable.
 */
public class SecureTokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateApiKey() {
        return "bbk_" + generateToken(32);
    }

    public static String generateSessionId() {
        return generateToken(24);
    }
}
