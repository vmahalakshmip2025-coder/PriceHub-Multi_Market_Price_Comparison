package com.pricehub.dto;

import com.pricehub.entity.enums.ProductCategory;
import java.math.BigDecimal;

/**
 * OOP: Immutable Record - High-level catalog summary for grid views.
 */
public record ProductSummaryDTO(
        Long id,
        String name,
        String brand,
        ProductCategory category,
        String categoryDisplayName,
        String imageUrl,
        BigDecimal lowestPrice,
        Double averageRating,
        boolean inStockAnywhere
) {
}