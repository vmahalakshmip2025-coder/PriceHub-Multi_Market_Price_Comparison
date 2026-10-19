package com.pricehub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * OOP: Immutable Record with validation constraints for updating marketplace prices.
 */
public record PriceUpdateRequest(
        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price,

        @Min(value = 0, message = "Rating must be at least 0.0")
        @Max(value = 5, message = "Rating cannot exceed 5.0")
        Double rating,

        Boolean inStock,
        String warranty,
        String productUrl,
        String imageUrl
) {
}