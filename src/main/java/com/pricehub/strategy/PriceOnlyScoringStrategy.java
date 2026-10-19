package com.pricehub.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * OOP: Strategy Pattern Implementation - Pure Price Evaluation.
 */
@Component
public class PriceOnlyScoringStrategy implements ValueScoringStrategy {

    @Override
    public double calculateScore(BigDecimal price, BigDecimal benchmarkPrice, Double rating) {
        if (price == null || benchmarkPrice == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double ratio = benchmarkPrice.divide(price, 4, RoundingMode.HALF_UP).doubleValue();
        return Math.min(ratio * 50.0, 100.0);
    }

    @Override
    public String getStrategyName() {
        return "Price-Only Scoring";
    }
}