package com.baileybakery.common.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template rendering engine for dynamic email, receipt, and notification content.
 * Supports simple variable interpolation and expression evaluation for computed fields
 * like order totals, discount calculations, and loyalty point conversions.
 */
public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);
    private static final Pattern EXPR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    /**
     * Renders a template string by replacing ${expression} placeholders.
     * Supports simple variable substitution and computed expressions
     * (e.g., ${price * quantity} for line item totals).
     *
     * @param template the template string with ${...} placeholders
     * @param variables map of variable names to values
     * @return the rendered string
     */
    public static String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        // Bind variables into the script engine for expression evaluation
        variables.forEach((key, value) -> engine.put(key, value));

        Matcher matcher = EXPR_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1);

            // Check if it's a simple variable reference first
            if (variables.containsKey(expression)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(
                        String.valueOf(variables.get(expression))));
            } else {
                // Evaluate as expression (e.g., "price * quantity - discount")
                try {
                    Object evalResult = engine.eval(expression);
                    matcher.appendReplacement(result, Matcher.quoteReplacement(
                            String.valueOf(evalResult)));
                } catch (ScriptException e) {
                    log.warn("Failed to evaluate expression: {}", expression);
                    matcher.appendReplacement(result, Matcher.quoteReplacement("${" + expression + "}"));
                }
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Renders a template with automatic HTML escaping for web output.
     * Use this for rendering content that will be displayed in browsers.
     */
    public static String renderHtml(String template, Map<String, Object> variables) {
        String rendered = render(template, variables);
        return escapeHtml(rendered);
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
