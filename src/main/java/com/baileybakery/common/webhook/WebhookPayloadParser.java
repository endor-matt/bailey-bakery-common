package com.baileybakery.common.webhook;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Entry point for parsing webhook bodies into {@link WebhookPayload}.
 *
 * <p>Dispatches by the caller-supplied {@code Content-Type} header:
 * YAML content types (e.g. {@code application/yaml}, {@code application/x-yaml},
 * {@code text/yaml}) are routed to {@link YamlPayloadParser}; everything else
 * (including {@code application/json} and unset content types) is handled by
 * the existing Gson-backed JSON path.</p>
 */
public class WebhookPayloadParser {

    private static final Logger log = LoggerFactory.getLogger(WebhookPayloadParser.class);
    private static final Type JSON_MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private static final String FIELD_EVENT_TYPE = "eventType";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_DATA = "data";

    private final Gson gson;
    private final YamlPayloadParser yamlPayloadParser;

    public WebhookPayloadParser() {
        this(new Gson(), new YamlPayloadParser());
    }

    WebhookPayloadParser(Gson gson, YamlPayloadParser yamlPayloadParser) {
        this.gson = gson;
        this.yamlPayloadParser = yamlPayloadParser;
    }

    /**
     * Parses a webhook body, dispatching by content type.
     *
     * @param body the raw webhook body; must not be {@code null}
     * @param contentType the value of the {@code Content-Type} header; may be
     *     {@code null}, in which case JSON is assumed
     * @return the parsed payload
     * @throws WebhookPayloadParseException if the body cannot be parsed
     */
    public WebhookPayload parse(String body, String contentType) {
        if (body == null) {
            throw new WebhookPayloadParseException("Webhook body is null");
        }
        if (isYaml(contentType)) {
            log.debug("Parsing webhook payload as YAML (content-type={})", contentType);
            return yamlPayloadParser.parse(body);
        }
        log.debug("Parsing webhook payload as JSON (content-type={})", contentType);
        return parseJson(body);
    }

    private WebhookPayload parseJson(String body) {
        Map<String, Object> rawMap;
        try {
            rawMap = gson.fromJson(body, JSON_MAP_TYPE);
        } catch (JsonSyntaxException e) {
            throw new WebhookPayloadParseException("Malformed JSON webhook body", e);
        }
        if (rawMap == null) {
            throw new WebhookPayloadParseException("JSON webhook body is empty");
        }

        Object eventType = rawMap.get(FIELD_EVENT_TYPE);
        if (!(eventType instanceof String eventTypeStr) || eventTypeStr.isEmpty()) {
            throw new WebhookPayloadParseException(
                    "JSON webhook body is missing required field: " + FIELD_EVENT_TYPE);
        }

        Object timestamp = rawMap.get(FIELD_TIMESTAMP);
        String timestampStr = timestamp == null ? null : timestamp.toString();

        Object dataNode = rawMap.get(FIELD_DATA);
        Map<String, Object> data;
        if (dataNode == null) {
            data = new LinkedHashMap<>();
        } else if (dataNode instanceof Map<?, ?> dataMap) {
            data = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                data.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        } else {
            throw new WebhookPayloadParseException(
                    "JSON webhook body field 'data' must be an object if present");
        }

        return new WebhookPayload(eventTypeStr, timestampStr, data);
    }

    private static boolean isYaml(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        int semi = normalized.indexOf(';');
        if (semi >= 0) {
            normalized = normalized.substring(0, semi);
        }
        normalized = normalized.trim();
        return normalized.equals("application/yaml")
                || normalized.equals("application/x-yaml")
                || normalized.equals("text/yaml")
                || normalized.equals("text/x-yaml")
                || normalized.endsWith("+yaml");
    }
}
