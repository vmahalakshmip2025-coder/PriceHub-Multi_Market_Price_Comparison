package com.pricehub.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * OOP: Strategy Pattern Implementation.
 * Weights: 60% Price Competitiveness + 40% Customer Rating.
 * If rating is absent, scores 100% on price ratio.
 */
@Component
public class GeneralScoringStrategy implements ValueScoringStrategy {

    private static final double PRICE_WEIGHT = 0.60;
    private static final double RATING_WEIGHT = 0.40;

    @Override
    public double calculateScore(BigDecimal price, BigDecimal benchmarkPrice, Double rating) {
        if (price == null || benchmarkPrice == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }

        // Relative price score: Lower price relative to benchmark earns higher score
        // Ratio > 1.0 means cheaper than benchmark, Ratio < 1.0 means more expensive
        double priceRatio = benchmarkPrice.divide(price, 4, RoundingMode.HALF_UP).doubleValue();
        double normalizedPriceScore = Math.min(priceRatio * 50.0, 100.0);

        if (rating == null || rating <= 0.0) {
            // Fallback: 100% price when rating is missing
            return normalizedPriceScore;
        }

        // Rating is out of 5.0 -> normalize to 100
        double normalizedRatingScore = (rating / 5.0) * 100.0;

        return (normalizedPriceScore * PRICE_WEIGHT) + (normalizedRatingScore * RATING_WEIGHT);
    }

    @Override
    public String getStrategyName() {
        return "General Value Scoring (60% Price, 40% Rating)";
    }
}