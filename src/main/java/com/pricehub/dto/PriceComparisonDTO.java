package com.pricehub.dto;

import com.pricehub.entity.enums.ProductCategory;
import java.math.BigDecimal;
import java.util.List;

/**
 * OOP: Immutable Record - Full comparison across all 3 marketplaces.
 */
public record PriceComparisonDTO(
        Long productId,
        String productName,
        String brand,
        ProductCategory category,
        String categoryDisplayName,
        String imageUrl,
        BigDecimal lowestPrice,
        BigDecimal highestPrice,
        BigDecimal savingsAmount,
        Double savingsPercentage,
        String cheapestMarketplace,
        BestValueDTO bestValue,
        List<MarketplacePriceDTO> marketplaces
) {
}