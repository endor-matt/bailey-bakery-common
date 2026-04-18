package com.baileybakery.common.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlPayloadParserTest {

    private final YamlPayloadParser parser = new YamlPayloadParser();

    @Test
    void parsesValidYamlPayload() {
        String yaml = """
                eventType: payment.completed
                timestamp: "2025-01-15T10:30:00Z"
                data:
                  orderId: 42
                  amount: 19.99
                  currency: USD
                """;

        WebhookPayload payload = parser.parse(yaml);

        assertNotNull(payload);
        assertEquals("payment.completed", payload.getEventType());
        assertEquals("2025-01-15T10:30:00Z", payload.getTimestamp());
        assertEquals(42, payload.getData().get("orderId"));
        assertEquals(19.99, payload.getData().get("amount"));
        assertEquals("USD", payload.getData().get("currency"));
    }

    @Test
    void parsesValidYamlPayloadWithoutOptionalFields() {
        String yaml = "eventType: delivery.status\n";

        WebhookPayload payload = parser.parse(yaml);

        assertEquals("delivery.status", payload.getEventType());
        assertNull(payload.getTimestamp());
        assertTrue(payload.getData().isEmpty());
    }

    @Test
    void rejectsMalformedYaml() {
        String malformed = "eventType: payment.completed\n  : : [unterminated";

        WebhookPayloadParseException ex = assertThrows(
                WebhookPayloadParseException.class, () -> parser.parse(malformed));

        assertTrue(ex.getMessage().toLowerCase().contains("malformed"),
                "Expected 'malformed' in message, got: " + ex.getMessage());
    }

    @Test
    void rejectsMissingRequiredEventType() {
        String yaml = """
                timestamp: "2025-01-15T10:30:00Z"
                data:
                  orderId: 42
                """;

        WebhookPayloadParseException ex = assertThrows(
                WebhookPayloadParseException.class, () -> parser.parse(yaml));

        assertTrue(ex.getMessage().contains("eventType"),
                "Expected 'eventType' in message, got: " + ex.getMessage());
    }

    @Test
    void rejectsNullBody() {
        assertThrows(WebhookPayloadParseException.class, () -> parser.parse(null));
    }
}
