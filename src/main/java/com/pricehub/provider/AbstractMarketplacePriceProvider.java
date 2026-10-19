package com.pricehub.provider;

import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceRepository;
import java.util.Objects;
import java.util.Optional;

/**
 * OOP: Template Method Pattern.
 * AbstractMarketplacePriceProvider defines the template workflow for retrieving
 * and normalizing marketplace price details, delegating platform-specific hooks
 * to concrete subclasses.
 */
public abstract class AbstractMarketplacePriceProvider implements MarketplacePriceProvider {

    protected final PriceRepository priceRepository;
    protected final MarketplaceType marketplaceType;

    // OOP: Constructor Injection
    protected AbstractMarketplacePriceProvider(PriceRepository priceRepository, MarketplaceType marketplaceType) {
        this.priceRepository = Objects.requireNonNull(priceRepository, "PriceRepository cannot be null");
        this.marketplaceType = Objects.requireNonNull(marketplaceType, "MarketplaceType cannot be null");
    }

    @Override
    public MarketplaceType getMarketplaceType() {
        return this.marketplaceType;
    }

    /**
     * OOP: Template Method.
     * Coordinates product validation, database retrieval, and data post-processing.
     */
    @Override
    public final Optional<Price> getPriceForProduct(Product product) {
        if (product == null || product.getId() == null) {
            return Optional.empty();
        }

        Optional<Price> optionalPrice = priceRepository.findByProductIdAndMarketplace(product.getId(), this.marketplaceType);
        
        // Hook for subclasses to post-process price entities if necessary
        return optionalPrice.map(this::postProcessPrice);
    }

    /**
     * OOP: Hook Method in Template Pattern. Subclasses may override to apply custom pricing logic.
     */
    protected Price postProcessPrice(Price price) {
        return price; // Default implementation leaves price unchanged
    }

    /**
     * Standard URL resolution: Uses stored URL or falls back to marketplace search URL.
     */
    @Override
    public String resolveProductUrl(Product product, Price price) {
        if (price != null && price.getProductUrl() != null && !price.getProductUrl().isBlank()) {
            return price.getProductUrl();
        }
        return this.marketplaceType.buildSearchUrl(product.getName());
    }

    @Override
    public String toString() {
        return "MarketplacePriceProvider[" + marketplaceType.getDisplayName() + "]";
    }
}