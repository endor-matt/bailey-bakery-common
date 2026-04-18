package com.baileybakery.common.webhook;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable representation of a parsed webhook payload.
 *
 * <p>Webhook senders (payment providers, delivery partners, supplier
 * notification systems) may post either JSON or YAML bodies; after parsing
 * the content is normalized into this type.</p>
 */
public class WebhookPayload {

    private final String eventType;
    private final String timestamp;
    private final Map<String, Object> data;

    public WebhookPayload(String eventType, String timestamp, Map<String, Object> data) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.data = data == null ? Collections.emptyMap() : Collections.unmodifiableMap(data);
    }

    public String getEventType() {
        return eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
