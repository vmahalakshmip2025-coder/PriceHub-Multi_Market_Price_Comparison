package com.pricehub.provider;

import com.pricehub.entity.Price;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceRepository;
import org.springframework.stereotype.Component;

/**
 * OOP: Concrete Strategy & Subclass implementation for Flipkart.
 */
@Component
public class FlipkartPriceProvider extends AbstractMarketplacePriceProvider {

    // OOP: Constructor Injection
    public FlipkartPriceProvider(PriceRepository priceRepository) {
        super(priceRepository, MarketplaceType.FLIPKART);
    }

    @Override
    protected Price postProcessPrice(Price price) {
        // Flipkart-specific post processing
        return super.postProcessPrice(price);
    }
}