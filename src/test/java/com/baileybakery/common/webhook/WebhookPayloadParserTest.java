package com.baileybakery.common.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebhookPayloadParserTest {

    private final WebhookPayloadParser parser = new WebhookPayloadParser();

    @Test
    void dispatchesJsonByDefault() {
        String json = "{\"eventType\":\"payment.completed\",\"data\":{\"orderId\":42}}";

        WebhookPayload payload = parser.parse(json, "application/json");

        assertEquals("payment.completed", payload.getEventType());
        assertEquals(42.0, payload.getData().get("orderId"));
    }

    @Test
    void dispatchesJsonWhenContentTypeIsNull() {
        String json = "{\"eventType\":\"delivery.status\"}";

        WebhookPayload payload = parser.parse(json, null);

        assertEquals("delivery.status", payload.getEventType());
    }

    @Test
    void dispatchesYamlForYamlContentType() {
        String yaml = "eventType: payment.completed\ndata:\n  orderId: 42\n";

        WebhookPayload payload = parser.parse(yaml, "application/yaml");

        assertEquals("payment.completed", payload.getEventType());
        assertEquals(42, payload.getData().get("orderId"));
    }

    @Test
    void dispatchesYamlIgnoringCharsetParameter() {
        String yaml = "eventType: delivery.status\n";

        WebhookPayload payload = parser.parse(yaml, "application/x-yaml; charset=utf-8");

        assertEquals("delivery.status", payload.getEventType());
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(WebhookPayloadParseException.class,
                () -> parser.parse("{not json", "application/json"));
    }
}
