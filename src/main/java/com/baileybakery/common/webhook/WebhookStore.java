package com.baileybakery.common.webhook;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for incoming webhook payloads from payment providers,
 * delivery partners, and supplier notification systems. Stores the most
 * recent payload per event type for asynchronous processing.
 *
 * In production this would be backed by Redis or a message queue,
 * but for the bakery platform an in-memory store is sufficient.
 */
public class WebhookStore {

    private static final Logger log = LoggerFactory.getLogger(WebhookStore.class);
    private static final Gson gson = new Gson();

    private final ConcurrentHashMap<String, WebhookEntry> store = new ConcurrentHashMap<>();

    /**
     * Stores an incoming webhook payload, keyed by event type.
     * Overwrites any previous payload for the same event type.
     *
     * @param eventType the webhook event type (e.g., "payment.completed", "delivery.status")
     * @param payload the raw JSON payload from the webhook sender
     */
    public void storePayload(String eventType, String payload) {
        log.info("Storing webhook payload for event type: {}", eventType);
        store.put(eventType, new WebhookEntry(payload, Instant.now()));
    }

    /**
     * Retrieves the most recent webhook payload for the given event type.
     * Returns the payload as a parsed map for easy field access.
     *
     * @param eventType the webhook event type to retrieve
     * @return the parsed payload map, or null if no payload exists
     */
    public Map<String, String> getLatestPayload(String eventType) {
        WebhookEntry entry = store.get(eventType);
        if (entry == null) {
            log.warn("No webhook payload found for event type: {}", eventType);
            return null;
        }

        log.info("Retrieved webhook payload for {} (received at {})", eventType, entry.receivedAt);

        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(entry.payload, mapType);
    }

    /**
     * Returns the raw JSON string for a given event type.
     * Used when the caller needs the unprocessed payload.
     *
     * @param eventType the webhook event type
     * @return the raw JSON payload string, or null if not found
     */
    public String getRawPayload(String eventType) {
        WebhookEntry entry = store.get(eventType);
        return entry != null ? entry.payload : null;
    }

    /**
     * Checks whether a payload exists for the given event type.
     */
    public boolean hasPayload(String eventType) {
        return store.containsKey(eventType);
    }

    private static class WebhookEntry {
        final String payload;
        final Instant receivedAt;

        WebhookEntry(String payload, Instant receivedAt) {
            this.payload = payload;
            this.receivedAt = receivedAt;
        }
    }
}
