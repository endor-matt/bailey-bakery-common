package com.baileybakery.common.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses webhook payloads encoded as YAML into {@link WebhookPayload} instances.
 *
 * <p>Uses SnakeYAML's {@link SafeConstructor} so the parser only instantiates
 * standard YAML scalar and collection types; arbitrary Java class tags are
 * rejected.</p>
 */
public class YamlPayloadParser {

    private static final Logger log = LoggerFactory.getLogger(YamlPayloadParser.class);

    private static final String FIELD_EVENT_TYPE = "eventType";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_DATA = "data";

    private final Yaml yaml;

    public YamlPayloadParser() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        this.yaml = new Yaml(new SafeConstructor(options));
    }

    /**
     * Parses a YAML-encoded webhook body.
     *
     * @param body the raw YAML body; must not be {@code null}
     * @return the parsed payload
     * @throws WebhookPayloadParseException if the body is malformed, not a
     *     mapping, or missing required fields
     */
    public WebhookPayload parse(String body) {
        if (body == null) {
            throw new WebhookPayloadParseException("YAML webhook body is null");
        }

        Object root;
        try {
            root = yaml.load(body);
        } catch (YAMLException e) {
            log.warn("Rejecting malformed YAML webhook body: {}", e.getMessage());
            throw new WebhookPayloadParseException("Malformed YAML webhook body", e);
        }

        if (!(root instanceof Map<?, ?> rawMap)) {
            throw new WebhookPayloadParseException(
                    "YAML webhook body must be a mapping at the top level");
        }

        Object eventType = rawMap.get(FIELD_EVENT_TYPE);
        if (!(eventType instanceof String eventTypeStr) || eventTypeStr.isEmpty()) {
            throw new WebhookPayloadParseException(
                    "YAML webhook body is missing required field: " + FIELD_EVENT_TYPE);
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
                    "YAML webhook body field 'data' must be a mapping if present");
        }

        return new WebhookPayload(eventTypeStr, timestampStr, data);
    }
}
