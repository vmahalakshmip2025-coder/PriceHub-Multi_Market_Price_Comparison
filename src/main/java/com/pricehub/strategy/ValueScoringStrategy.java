package com.pricehub.strategy;

import java.math.BigDecimal;

/**
 * OOP: Strategy Pattern Interface.
 * Defines algorithm contract for calculating multi-factor value scores.
 */
public interface ValueScoringStrategy {

    /**
     * Calculates a normalized value score (0 - 100).
     *
     * @param price           The product price
     * @param benchmarkPrice  The reference product price
     * @param rating          The customer rating (0.0 - 5.0, nullable)
     * @return composite score between 0.0 and 100.0
     */
    double calculateScore(BigDecimal price, BigDecimal benchmarkPrice, Double rating);

    /**
     * Returns the name of the strategy.
     */
    String getStrategyName();
}