package com.pricehub.entity.enums;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OOP: Enum Encapsulation - Enums in Java can have fields, constructors, and methods.
 * Represents the e-commerce platforms supported by PriceHub.
 */
public enum MarketplaceType {
    AMAZON("Amazon", "https://www.amazon.in", "https://www.amazon.in/s?k="),
    FLIPKART("Flipkart", "https://www.flipkart.com", "https://www.flipkart.com/search?q="),
    MEESHO("Meesho", "https://www.meesho.com", "https://www.meesho.com/search?q=");

    private final String displayName;
    private final String baseUrl;
    private final String searchPrefix;

    MarketplaceType(String displayName, String baseUrl, String searchPrefix) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.searchPrefix = searchPrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Builds fallback search URL when a direct product link is unavailable.
     */
    public String buildSearchUrl(String productName) {
        if (productName == null || productName.isBlank()) {
            return baseUrl;
        }
        return searchPrefix + URLEncoder.encode(productName.trim(), StandardCharsets.UTF_8);
    }

    /**
     * Case-insensitive marketplace resolution.
     */
    public static MarketplaceType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (MarketplaceType type : MarketplaceType.values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return null;
    }
}