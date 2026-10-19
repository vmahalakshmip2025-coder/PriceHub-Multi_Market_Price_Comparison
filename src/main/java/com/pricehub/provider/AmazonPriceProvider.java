package com.pricehub.provider;

import com.pricehub.entity.Price;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceRepository;
import org.springframework.stereotype.Component;

/**
 * OOP: Concrete Strategy & Subclass implementation for Amazon.
 */
@Component
public class AmazonPriceProvider extends AbstractMarketplacePriceProvider {

    // OOP: Constructor Injection
    public AmazonPriceProvider(PriceRepository priceRepository) {
        super(priceRepository, MarketplaceType.AMAZON);
    }

    @Override
    protected Price postProcessPrice(Price price) {
        // Amazon-specific warranty defaults if empty
        if (price.getWarranty() == null || price.getWarranty().isBlank()) {
            price.setWarranty("Standard Manufacturer Warranty");
        }
        return price;
    }
}