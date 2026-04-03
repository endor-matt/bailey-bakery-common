package com.baileybakery.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses XML and YAML configuration files used across bakery services.
 * Handles supplier integration configs, menu definitions, and
 * deployment-specific settings.
 */
public class ConfigParser {

    private static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    /**
     * Parses an XML configuration string and returns a flat key-value map.
     * Used for processing supplier data feeds (ingredient prices, availability)
     * and importing menu configurations from the legacy system.
     *
     * @param xmlContent the XML string to parse
     * @return map of configuration keys to values
     */
    public static Map<String, String> parseXml(String xmlContent) throws Exception {
        Map<String, String> config = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

        Element root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element element) {
                config.put(element.getTagName(), element.getTextContent());
            }
        }

        log.info("Parsed {} configuration entries", config.size());
        return config;
    }

    /**
     * Parses an XML file from the filesystem.
     *
     * @param filePath path to the XML file
     * @return map of configuration keys to values
     */
    public static Map<String, String> parseXmlFile(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(filePath);

        Map<String, String> config = new HashMap<>();
        Element root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element element) {
                config.put(element.getTagName(), element.getTextContent());
            }
        }

        return config;
    }

    /**
     * Extracts a specific value from an XML configuration string.
     *
     * @param xmlContent the XML string
     * @param key the element tag name to find
     * @return the text content of the element, or null if not found
     */
    public static String getXmlValue(String xmlContent, String key) throws Exception {
        Map<String, String> config = parseXml(xmlContent);
        return config.get(key);
    }
}
