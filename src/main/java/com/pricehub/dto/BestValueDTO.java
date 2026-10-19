package com.pricehub.dto;

import com.pricehub.entity.enums.MarketplaceType;
import java.math.BigDecimal;

/**
 * OOP: Immutable Record - Best Value evaluation result.
 */
public record BestValueDTO(
        MarketplaceType marketplace,
        String displayName,
        BigDecimal price,
        Double rating,
        String warranty,
        String reason,
        BigDecimal savingsAgainstHighest
) {
}