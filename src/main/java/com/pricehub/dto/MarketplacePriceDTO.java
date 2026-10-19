package com.pricehub.dto;

import com.pricehub.entity.enums.MarketplaceType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OOP: Immutable Record - DTO representing a single marketplace's offering.
 */
public record MarketplacePriceDTO(
        MarketplaceType marketplace,
        String displayName,
        BigDecimal price,
        String formattedPrice,
        Double rating,
        boolean inStock,
        String warranty,
        LocalDate priceDate,
        String productUrl,
        String imageUrl,
        boolean isCheapest,
        boolean isBestValue
) {
}