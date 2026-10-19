package com.pricehub.service;

import com.pricehub.dto.AlternativeRecommendationDTO;
import com.pricehub.entity.Earphones;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.entity.enums.ProductCategory;
import com.pricehub.repository.PriceRepository;
import com.pricehub.repository.ProductRepository;
import com.pricehub.strategy.GeneralScoringStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlternativeAdvisorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceRepository priceRepository;

    private AlternativeAdvisorService advisorService;

    private Product targetEarphones;
    private Product peer1;
    private Product peer2;

    @BeforeEach
    void setUp() {
        advisorService = new AlternativeAdvisorService(productRepository, priceRepository, new GeneralScoringStrategy());

        targetEarphones = new Earphones("Wireless Bluetooth Neckband Earphones");
        targetEarphones.setId(1L);

        peer1 = new Earphones("True Wireless Earbuds with Charging Case");
        peer1.setId(2L);

        peer2 = new Earphones("Wired In-Ear Earphones with Mic");
        peer2.setId(3L);
    }

    @Test
    @DisplayName("Should recommend alternatives within +/-50% price in same category, ranked by score")
    void testGetAlternativesWithinPriceRange() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(targetEarphones));
        when(productRepository.findByCategory(ProductCategory.EARPHONES))
                .thenReturn(List.of(targetEarphones, peer1, peer2));

        // Target: ₹400, 4.0 rating
        Price targetPrice = new Price(targetEarphones, MarketplaceType.AMAZON, new BigDecimal("400.00"), 4.0, true, "1 Year", null, null, LocalDate.now());
        when(priceRepository.findByProductId(1L)).thenReturn(List.of(targetPrice));

        // Peer 1: ₹350 (within 50%), 4.5 rating -> high score
        Price peer1Price = new Price(peer1, MarketplaceType.AMAZON, new BigDecimal("350.00"), 4.5, true, "1 Year", null, null, LocalDate.now());
        when(priceRepository.findByProductId(2L)).thenReturn(List.of(peer1Price));

        // Peer 2: ₹250 (within 50%), 4.1 rating -> qualifying score
        Price peer2Price = new Price(peer2, MarketplaceType.FLIPKART, new BigDecimal("250.00"), 4.1, true, "6 Months", null, null, LocalDate.now());
        when(priceRepository.findByProductId(3L)).thenReturn(List.of(peer2Price));

        List<AlternativeRecommendationDTO> results = advisorService.getAlternatives(1L);

        assertFalse(results.isEmpty());
        // Max 3 suggestions
        assertTrue(results.size() <= 3);
        // Excludes target product itself
        assertTrue(results.stream().noneMatch(r -> r.productId().equals(1L)));
        // Should not be marked as fallback
        assertFalse(results.get(0).isFallbackOption());
    }

    @Test
    @DisplayName("Should return category fallback when peers fall outside +/- 50% price boundary")
    void testFallbackWhenNoPeerQualifies() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(targetEarphones));
        when(productRepository.findByCategory(ProductCategory.EARPHONES))
                .thenReturn(List.of(targetEarphones, peer1));

        // Target: ₹100
        Price targetPrice = new Price(targetEarphones, MarketplaceType.AMAZON, new BigDecimal("100.00"), 4.0, true, "1 Year", null, null, LocalDate.now());
        when(priceRepository.findByProductId(1L)).thenReturn(List.of(targetPrice));

        // Peer 1: ₹900 (far outside +/- 50%)
        Price peer1Price = new Price(peer1, MarketplaceType.AMAZON, new BigDecimal("900.00"), 4.8, true, "1 Year", null, null, LocalDate.now());
        when(priceRepository.findByProductId(2L)).thenReturn(List.of(peer1Price));

        List<AlternativeRecommendationDTO> results = advisorService.getAlternatives(1L);

        assertFalse(results.isEmpty());
        // Must return peer1 as category fallback
        assertTrue(results.get(0).isFallbackOption());
        assertEquals("Other option in this category", results.get(0).reason());
    }
}