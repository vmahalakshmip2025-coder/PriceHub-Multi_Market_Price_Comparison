package com.pricehub.provider;

import com.pricehub.entity.Price;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceRepository;
import org.springframework.stereotype.Component;

/**
 * OOP: Concrete Strategy & Subclass implementation for Meesho.
 */
@Component
public class MeeshoPriceProvider extends AbstractMarketplacePriceProvider {

    // OOP: Constructor Injection
    public MeeshoPriceProvider(PriceRepository priceRepository) {
        super(priceRepository, MarketplaceType.MEESHO);
    }

    @Override
    protected Price postProcessPrice(Price price) {
        // Meesho listings often do not include manufacturer warranty
        if (price.getWarranty() == null || price.getWarranty().isBlank()) {
            price.setWarranty("No Warranty");
        }
        return price;
    }
}