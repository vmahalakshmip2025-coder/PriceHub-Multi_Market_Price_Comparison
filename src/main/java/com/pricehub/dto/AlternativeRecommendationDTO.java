package com.pricehub.dto;

import com.pricehub.entity.enums.ProductCategory;
import java.math.BigDecimal;

/**
 * OOP: Immutable Record - Smart alternative suggestion.
 */
public record AlternativeRecommendationDTO(
        Long productId,
        String productName,
        ProductCategory category,
        String imageUrl,
        String cheapestMarketplace,
        BigDecimal lowestPrice,
        Double rating,
        double score,
        String reason,
        boolean isFallbackOption
) {
}