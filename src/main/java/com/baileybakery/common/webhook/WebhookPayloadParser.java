package com.baileybakery.common.webhook;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parses incoming webhook JSON payloads from upstream integrations (payment
 * providers, delivery partners, supplier notifications, etc.) into a structured
 * {@link WebhookPayload} object.
 *
 * <p>Parsing uses Gson, which is already a direct dependency of this library,
 * so no new JSON parser is introduced.
 *
 * <p>A well-formed payload must contain the following top-level fields:
 * <ul>
 *   <li>{@code event_type} (string, non-empty)</li>
 *   <li>{@code event_id} (string, non-empty)</li>
 *   <li>{@code timestamp} (ISO-8601 string, e.g. {@code 2024-05-01T12:34:56Z})</li>
 *   <li>{@code data} (JSON object, possibly empty)</li>
 * </ul>
 *
 * <p>Parsing failures throw {@link WebhookPayloadException}, which distinguishes
 * between malformed JSON ({@code cause} is a {@link JsonSyntaxException}) and
 * missing/invalid required fields.
 */
public class WebhookPayloadParser {

    private static final Logger log = LoggerFactory.getLogger(WebhookPayloadParser.class);
    private static final List<String> REQUIRED_FIELDS =
            List.of("event_type", "event_id", "timestamp", "data");

    private final Gson gson;

    public WebhookPayloadParser() {
        this(new Gson());
    }

    WebhookPayloadParser(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    /**
     * Parses the given raw JSON into a {@link WebhookPayload}.
     *
     * @param rawJson the raw JSON string received from the webhook sender
     * @return the parsed payload
     * @throws WebhookPayloadException if the JSON is null/empty, malformed,
     *     missing a required field, or has a field with an invalid type/value
     */
    public WebhookPayload parse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new WebhookPayloadException("Webhook payload is empty");
        }

        JsonElement root;
        try {
            root = JsonParser.parseString(rawJson);
        } catch (JsonSyntaxException e) {
            log.warn("Malformed webhook JSON payload: {}", e.getMessage());
            throw new WebhookPayloadException("Malformed JSON payload", e);
        }

        if (root == null || !root.isJsonObject()) {
            throw new WebhookPayloadException("Webhook payload must be a JSON object");
        }
        JsonObject obj = root.getAsJsonObject();

        for (String field : REQUIRED_FIELDS) {
            if (!obj.has(field) || obj.get(field).isJsonNull()) {
                throw new WebhookPayloadException(
                        "Missing required field: " + field);
            }
        }

        String eventType = requireNonEmptyString(obj, "event_type");
        String eventId = requireNonEmptyString(obj, "event_id");
        Instant timestamp = parseTimestamp(requireNonEmptyString(obj, "timestamp"));

        JsonElement dataEl = obj.get("data");
        if (!dataEl.isJsonObject()) {
            throw new WebhookPayloadException("Field 'data' must be a JSON object");
        }
        Map<String, Object> data = gson.fromJson(dataEl, LinkedHashMap.class);
        if (data == null) {
            data = new LinkedHashMap<>();
        }

        log.info("Parsed webhook payload event_type={} event_id={}", eventType, eventId);
        return new WebhookPayload(eventType, eventId, timestamp, Collections.unmodifiableMap(data));
    }

    private static String requireNonEmptyString(JsonObject obj, String field) {
        JsonElement el = obj.get(field);
        if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) {
            throw new WebhookPayloadException("Field '" + field + "' must be a string");
        }
        String value = el.getAsString();
        if (value.isBlank()) {
            throw new WebhookPayloadException("Field '" + field + "' must not be blank");
        }
        return value;
    }

    private static Instant parseTimestamp(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new WebhookPayloadException(
                    "Field 'timestamp' is not a valid ISO-8601 instant: " + value, e);
        }
    }

    /** Immutable value object representing a parsed webhook payload. */
    public static final class WebhookPayload {
        private final String eventType;
        private final String eventId;
        private final Instant timestamp;
        private final Map<String, Object> data;

        public WebhookPayload(String eventType, String eventId, Instant timestamp,
                              Map<String, Object> data) {
            this.eventType = eventType;
            this.eventId = eventId;
            this.timestamp = timestamp;
            this.data = data;
        }

        public String getEventType() { return eventType; }
        public String getEventId() { return eventId; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return data; }
    }

    /** Thrown when a webhook payload cannot be parsed or is missing required fields. */
    public static class WebhookPayloadException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public WebhookPayloadException(String message) {
            super(message);
        }

        public WebhookPayloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
