package com.pricehub.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringStrategyTest {

    private GeneralScoringStrategy generalStrategy;
    private PriceOnlyScoringStrategy priceOnlyStrategy;

    @BeforeEach
    void setUp() {
        generalStrategy = new GeneralScoringStrategy();
        priceOnlyStrategy = new PriceOnlyScoringStrategy();
    }

    @Test
    @DisplayName("General strategy: Cheaper item with higher rating scores higher")
    void testGeneralStrategyEvaluation() {
        BigDecimal benchmark = new BigDecimal("1000.00");

        // Candidate A: ₹800 (cheaper), 4.5 rating
        double scoreA = generalStrategy.calculateScore(new BigDecimal("800.00"), benchmark, 4.5);

        // Candidate B: ₹1200 (more expensive), 3.5 rating
        double scoreB = generalStrategy.calculateScore(new BigDecimal("1200.00"), benchmark, 3.5);

        assertTrue(scoreA > scoreB, "Cheaper product with better rating must score higher");
    }

    @Test
    @DisplayName("General strategy: Fallback to price-only when rating is null")
    void testGeneralStrategyNullRating() {
        BigDecimal benchmark = new BigDecimal("500.00");
        BigDecimal price = new BigDecimal("500.00");

        double score = generalStrategy.calculateScore(price, benchmark, null);
        assertEquals(50.0, score, 0.01, "At equal price with no rating, normalized price score is 50.0");
    }

    @Test
    @DisplayName("Price-only strategy: Computes score purely on price ratio")
    void testPriceOnlyStrategy() {
        BigDecimal benchmark = new BigDecimal("1000.00");
        double scoreEqual = priceOnlyStrategy.calculateScore(new BigDecimal("1000.00"), benchmark, null);
        double scoreCheaper = priceOnlyStrategy.calculateScore(new BigDecimal("500.00"), benchmark, null);

        assertEquals(50.0, scoreEqual, 0.01);
        assertEquals(100.0, scoreCheaper, 0.01);
    }
}