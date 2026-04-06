package com.baileybakery.common.feed;

import com.baileybakery.common.http.ServiceClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for fetching real-time data feeds from external supplier and partner APIs.
 * Used by the platform service to synchronize supplier pricing, inventory levels,
 * and catalog updates from third-party sources.
 */
public class DataFeedClient {

    private static final Logger log = LoggerFactory.getLogger(DataFeedClient.class);
    private static final Gson gson = new Gson();

    private final String feedBaseUrl;

    public DataFeedClient(String feedBaseUrl) {
        this.feedBaseUrl = feedBaseUrl;
    }

    /**
     * Fetches the latest supplier pricing data from the external feed API.
     * Returns a list of price update records, each containing supplier name,
     * item category, and new price values.
     *
     * @param supplierId the supplier whose prices to fetch
     * @return list of pricing records from the external API response
     */
    public List<Map<String, String>> fetchSupplierPrices(String supplierId) {
        try {
            String url = feedBaseUrl + "/api/suppliers/" + supplierId + "/prices";
            log.info("Fetching supplier prices from feed: {}", url);

            String response = ServiceClient.get(url);

            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> prices = gson.fromJson(response, listType);

            log.info("Received {} price records for supplier {}", prices.size(), supplierId);
            return prices;
        } catch (IOException e) {
            log.error("Failed to fetch supplier prices for: {}", supplierId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches catalog updates from a partner integration endpoint.
     * Returns the raw JSON response for the caller to process.
     *
     * @param partnerId the partner whose catalog to fetch
     * @return raw JSON string from the partner API
     */
    public String fetchCatalogUpdates(String partnerId) {
        try {
            String url = feedBaseUrl + "/api/partners/" + partnerId + "/catalog";
            log.info("Fetching catalog updates from: {}", url);
            return ServiceClient.get(url);
        } catch (IOException e) {
            log.error("Failed to fetch catalog for partner: {}", partnerId, e);
            return "{}";
        }
    }
}
