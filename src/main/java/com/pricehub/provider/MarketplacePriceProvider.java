package com.pricehub.provider;

import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import java.util.Optional;

/**
 * OOP: Interface Definition.
 * Establishes a common behavioral contract for interacting with diverse marketplaces.
 */
public interface MarketplacePriceProvider {

    /**
     * Gets the marketplace type handled by this provider.
     */
    MarketplaceType getMarketplaceType();

    /**
     * Retrieves the current price record for a given product on this marketplace.
     */
    Optional<Price> getPriceForProduct(Product product);

    /**
     * Generates the product buy link, falling back to a search query URL if absent.
     */
    String resolveProductUrl(Product product, Price price);

    /**
     * Checks if this provider can handle the given marketplace type.
     */
    default boolean supports(MarketplaceType marketplaceType) {
        return getMarketplaceType() == marketplaceType;
    }
}