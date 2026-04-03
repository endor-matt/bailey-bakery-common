package com.baileybakery.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Structured audit logging for compliance and security monitoring.
 * Writes audit events to the application log in a parseable format
 * that feeds into the ELK stack for real-time alerting.
 */
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    /**
     * Logs an audit event with action, actor, and details.
     * Used for tracking order modifications, payment events,
     * admin actions, and authentication attempts.
     *
     * @param action the action performed (e.g., "ORDER_CREATED", "LOGIN_ATTEMPT")
     * @param actor the user or service performing the action
     * @param details additional context about the event
     */
    public static void logEvent(String action, String actor, String details) {
        String timestamp = Instant.now().toString();
        log.info("AUDIT|{}|{}|{}|{}", timestamp, action, actor, details);
    }

    /**
     * Logs an audit event with structured metadata.
     *
     * @param action the action performed
     * @param actor the user or service
     * @param metadata key-value pairs of event context
     */
    public static void logEvent(String action, String actor, Map<String, String> metadata) {
        StringBuilder details = new StringBuilder();
        metadata.forEach((k, v) -> details.append(k).append("=").append(v).append(";"));

        logEvent(action, actor, details.toString());
    }

    /**
     * Logs a security-relevant event (failed login, privilege escalation attempt, etc.)
     * These events trigger real-time alerts in the monitoring pipeline.
     *
     * @param action the security event type
     * @param actor the user or IP address
     * @param details description of the event
     * @param severity one of: LOW, MEDIUM, HIGH, CRITICAL
     */
    public static void logSecurityEvent(String action, String actor, String details, String severity) {
        String timestamp = Instant.now().toString();
        log.warn("SECURITY|{}|{}|{}|{}|{}", timestamp, severity, action, actor, details);
    }
}
