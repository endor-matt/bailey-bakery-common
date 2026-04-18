package com.baileybakery.common.webhook;

/**
 * Thrown when a webhook body cannot be parsed into a {@link WebhookPayload},
 * either because the body is malformed or because required fields are missing.
 */
public class WebhookPayloadParseException extends RuntimeException {

    public WebhookPayloadParseException(String message) {
        super(message);
    }

    public WebhookPayloadParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
