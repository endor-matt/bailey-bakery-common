package com.baileybakery.common.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP client wrapper for inter-service communication within the Bailey Bakery platform.
 * Used for calling inventory, payment, and delivery partner APIs.
 */
public class ServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceClient.class);

    /**
     * Sends a GET request to the specified URL and returns the response body.
     * Used for fetching data from internal services and external partner APIs.
     *
     * @param url the target URL
     * @return the response body as a string
     */
    public static String get(String url) throws IOException {
        log.info("GET request to: {}", url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Service", "bailey-bakery");

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    /**
     * Sends a POST request with a JSON body to the specified URL.
     * Used for creating orders, triggering webhooks, and sending notifications.
     *
     * @param url the target URL
     * @param jsonBody the JSON request body
     * @return the response body as a string
     */
    public static String post(String url, String jsonBody) throws IOException {
        log.info("POST request to: {}", url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Service", "bailey-bakery");
            request.setEntity(new StringEntity(jsonBody));

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    /**
     * Sends a GET request with custom headers. Used for authenticated partner API calls.
     *
     * @param url the target URL
     * @param headers map of header name to value
     * @return the response body as a string
     */
    public static String getWithHeaders(String url, Map<String, String> headers) throws IOException {
        log.info("GET request with custom headers to: {}", url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            headers.forEach(request::setHeader);

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}
