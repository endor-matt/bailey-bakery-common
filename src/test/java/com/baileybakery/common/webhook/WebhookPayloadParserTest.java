package com.baileybakery.common.webhook;

import com.baileybakery.common.webhook.WebhookPayloadParser.WebhookPayload;
import com.baileybakery.common.webhook.WebhookPayloadParser.WebhookPayloadException;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookPayloadParserTest {

    private final WebhookPayloadParser parser = new WebhookPayloadParser();

    @Test
    void parsesValidPayload() {
        String json = "{"
                + "\"event_type\":\"payment.completed\","
                + "\"event_id\":\"evt_123\","
                + "\"timestamp\":\"2024-05-01T12:34:56Z\","
                + "\"data\":{\"order_id\":\"ord_42\",\"amount\":1999}"
                + "}";

        WebhookPayload payload = parser.parse(json);

        assertNotNull(payload);
        assertEquals("payment.completed", payload.getEventType());
        assertEquals("evt_123", payload.getEventId());
        assertEquals(Instant.parse("2024-05-01T12:34:56Z"), payload.getTimestamp());
        assertEquals("ord_42", payload.getData().get("order_id"));
        // Gson decodes numeric JSON values as Double when target is Map<String,Object>.
        assertEquals(1999.0, payload.getData().get("amount"));
    }

    @Test
    void rejectsMalformedJson() {
        String malformed = "{\"event_type\": \"payment.completed\", "; // truncated

        WebhookPayloadException ex = assertThrows(
                WebhookPayloadException.class, () -> parser.parse(malformed));

        assertTrue(ex.getMessage().toLowerCase().contains("malformed"),
                "expected malformed-JSON message, got: " + ex.getMessage());
        assertInstanceOf(JsonSyntaxException.class, ex.getCause());
    }

    @Test
    void rejectsPayloadMissingRequiredField() {
        // 'timestamp' is missing.
        String json = "{"
                + "\"event_type\":\"payment.completed\","
                + "\"event_id\":\"evt_123\","
                + "\"data\":{\"order_id\":\"ord_42\"}"
                + "}";

        WebhookPayloadException ex = assertThrows(
                WebhookPayloadException.class, () -> parser.parse(json));

        assertTrue(ex.getMessage().contains("timestamp"),
                "expected message to mention missing 'timestamp', got: " + ex.getMessage());
    }
}
