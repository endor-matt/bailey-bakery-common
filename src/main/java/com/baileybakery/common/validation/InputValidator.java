package com.baileybakery.common.validation;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Input validation utilities shared across bakery services.
 * Provides reusable validation for common input types.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{6,14}$");
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "inventory-service", "payment-service", "delivery-service",
            "api.baileybakery.com", "cdn.baileybakery.com");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates that a URL points to an allowed internal service.
     */
    public static boolean isAllowedServiceUrl(String url) {
        try {
            URI uri = new URI(url);
            return ALLOWED_HOSTS.contains(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that a file path does not escape the base directory.
     */
    public static boolean isSafePath(String basePath, String relativePath) {
        try {
            Path base = Paths.get(basePath).toAbsolutePath().normalize();
            Path resolved = base.resolve(relativePath).normalize();
            return resolved.startsWith(base);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitizes a string for safe inclusion in log messages.
     */
    public static String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n]", " ").replaceAll("[^\\x20-\\x7E]", "?");
    }
}
