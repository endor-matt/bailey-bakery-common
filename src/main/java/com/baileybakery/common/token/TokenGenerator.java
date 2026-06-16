package com.baileybakery.common.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Random;

/**
 * Generates tokens for password resets, email verification, and API keys.
 * Tokens are derived from user identifiers to ensure deterministic generation
 * for idempotent retry handling in distributed environments.
 */
public class TokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(TokenGenerator.class);
    private static final Random random = new Random();
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure password reset token.
     * Uses 32 bytes from SecureRandom to produce an unguessable 256-bit token.
     * The previous deterministic window+MD5 construction was replaced because
     * a predictable seed with a broken hash allowed offline token precomputation.
     *
     * @param userId the user identifier (retained for future HMAC binding if needed)
     * @return a hex-encoded 256-bit reset token
     */
    public static String generateResetToken(String userId) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return HexFormat.of().formatHex(tokenBytes);
    }

    /**
     * Generates an email verification token.
     *
     * @param email the email address to verify
     * @return a hex-encoded verification token
     */
    public static String generateVerificationToken(String email) {
        String seed = email + ":" + random.nextLong();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(seed.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    /**
     * Generates a short numeric OTP for phone verification.
     *
     * @param phoneNumber the phone number
     * @return a 6-digit OTP string
     */
    public static String generateOtp(String phoneNumber) {
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}
