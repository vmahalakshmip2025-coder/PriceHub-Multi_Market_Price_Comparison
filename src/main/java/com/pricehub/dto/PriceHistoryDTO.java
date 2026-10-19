package com.pricehub.dto;

import com.pricehub.entity.enums.MarketplaceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OOP: Immutable Record - Historical audit entry.
 */
public record PriceHistoryDTO(
        Long id,
        MarketplaceType marketplace,
        String marketplaceName,
        BigDecimal price,
        boolean inStock,
        Double rating,
        LocalDateTime recordedAt
) {
}