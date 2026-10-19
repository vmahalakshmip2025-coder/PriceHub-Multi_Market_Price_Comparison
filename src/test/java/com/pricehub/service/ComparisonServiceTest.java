package com.pricehub.service;

import com.pricehub.dto.PriceComparisonDTO;
import com.pricehub.entity.Earphones;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.provider.MarketplacePriceProvider;
import com.pricehub.repository.ProductRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MarketplacePriceProvider amazonProvider;

    @Mock
    private MarketplacePriceProvider flipkartProvider;

    @Mock
    private MarketplacePriceProvider meeshoProvider;

    private ComparisonService comparisonService;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        when(amazonProvider.supports(MarketplaceType.AMAZON)).thenReturn(true);
        when(flipkartProvider.supports(MarketplaceType.FLIPKART)).thenReturn(true);
        when(meeshoProvider.supports(MarketplaceType.MEESHO)).thenReturn(true);

        when(amazonProvider.resolveProductUrl(any(), any())).thenReturn("https://amazon.in/p");
        when(flipkartProvider.resolveProductUrl(any(), any())).thenReturn("https://flipkart.com/p");
        when(meeshoProvider.resolveProductUrl(any(), any())).thenReturn("https://meesho.com/p");

        comparisonService = new ComparisonService(
                productRepository,
                List.of(amazonProvider, flipkartProvider, meeshoProvider)
        );

        sampleProduct = new Earphones("Wireless Bluetooth Neckband Earphones");
        sampleProduct.setId(1L);
    }

    @Test
    @DisplayName("Should correctly calculate lowest, highest, and savings amounts/percentages")
    void testSavingsCalculation() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Price pAmazon = new Price(sampleProduct, MarketplaceType.AMAZON, new BigDecimal("400.00"), 4.0, true, "1 Year", null, null, LocalDate.now());
        Price pFlipkart = new Price(sampleProduct, MarketplaceType.FLIPKART, new BigDecimal("500.00"), 4.1, true, "6 Months", null, null, LocalDate.now());
        Price pMeesho = new Price(sampleProduct, MarketplaceType.MEESHO, new BigDecimal("250.00"), 3.8, true, "No Warranty", null, null, LocalDate.now());

        when(amazonProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pAmazon));
        when(flipkartProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pFlipkart));
        when(meeshoProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pMeesho));

        PriceComparisonDTO comparison = comparisonService.compareProduct(1L);

        assertEquals(new BigDecimal("250.00"), comparison.lowestPrice());
        assertEquals(new BigDecimal("500.00"), comparison.highestPrice());
        assertEquals(new BigDecimal("250.00"), comparison.savingsAmount()); // 500 - 250
        assertEquals(50.0, comparison.savingsPercentage(), 0.01); // (250 / 500) * 100
        assertEquals("Meesho", comparison.cheapestMarketplace());
    }

    @Test
    @DisplayName("Best Value Rule 1: Recommends cheapest candidate by default")
    void testBestValueCheapestDefault() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Price pAmazon = new Price(sampleProduct, MarketplaceType.AMAZON, new BigDecimal("350.00"), 4.0, true, "1 Year", null, null, LocalDate.now());
        Price pFlipkart = new Price(sampleProduct, MarketplaceType.FLIPKART, new BigDecimal("450.00"), 4.2, true, "1 Year", null, null, LocalDate.now());
        Price pMeesho = new Price(sampleProduct, MarketplaceType.MEESHO, new BigDecimal("200.00"), 3.9, true, "No Warranty", null, null, LocalDate.now());

        when(amazonProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pAmazon));
        when(flipkartProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pFlipkart));
        when(meeshoProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pMeesho));

        PriceComparisonDTO comparison = comparisonService.compareProduct(1L);

        assertNotNull(comparison.bestValue());
        assertEquals(MarketplaceType.MEESHO, comparison.bestValue().marketplace());
        assertEquals(new BigDecimal("200.00"), comparison.bestValue().price());
    }

    @Test
    @DisplayName("Best Value Rule 2: Recommends option within 5% price if rating is >= 0.5 higher")
    void testBestValueUpgradeCondition() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // Meesho is ₹1000 with 3.5 rating
        Price pMeesho = new Price(sampleProduct, MarketplaceType.MEESHO, new BigDecimal("1000.00"), 3.5, true, "No Warranty", null, null, LocalDate.now());
        // Amazon is ₹1040 (within 5% of ₹1000, max allowed is ₹1050) but rating is 4.3 (0.8 higher)
        Price pAmazon = new Price(sampleProduct, MarketplaceType.AMAZON, new BigDecimal("1040.00"), 4.3, true, "1 Year", null, null, LocalDate.now());
        Price pFlipkart = new Price(sampleProduct, MarketplaceType.FLIPKART, new BigDecimal("1200.00"), 4.2, true, "1 Year", null, null, LocalDate.now());

        when(amazonProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pAmazon));
        when(flipkartProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pFlipkart));
        when(meeshoProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pMeesho));

        PriceComparisonDTO comparison = comparisonService.compareProduct(1L);

        assertNotNull(comparison.bestValue());
        // Best value should be Amazon despite Meesho being cheaper
        assertEquals(MarketplaceType.AMAZON, comparison.bestValue().marketplace());
        assertEquals(new BigDecimal("1040.00"), comparison.bestValue().price());
        assertTrue(comparison.bestValue().reason().contains("significantly better rating"));
    }

    @Test
    @DisplayName("Best Value Rule 3: Breaks price tie using warranty duration")
    void testBestValueTieBreakingByWarranty() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // Both Amazon and Flipkart are ₹500 with same rating, but Amazon has 2 Years vs Flipkart 6 Months
        Price pAmazon = new Price(sampleProduct, MarketplaceType.AMAZON, new BigDecimal("500.00"), 4.0, true, "2 Years", null, null, LocalDate.now());
        Price pFlipkart = new Price(sampleProduct, MarketplaceType.FLIPKART, new BigDecimal("500.00"), 4.0, true, "6 Months", null, null, LocalDate.now());
        Price pMeesho = new Price(sampleProduct, MarketplaceType.MEESHO, new BigDecimal("550.00"), 4.0, true, "No Warranty", null, null, LocalDate.now());

        when(amazonProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pAmazon));
        when(flipkartProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pFlipkart));
        when(meeshoProvider.getPriceForProduct(sampleProduct)).thenReturn(Optional.of(pMeesho));

        PriceComparisonDTO comparison = comparisonService.compareProduct(1L);

        assertNotNull(comparison.bestValue());
        assertEquals(MarketplaceType.AMAZON, comparison.bestValue().marketplace());
        assertEquals("2 Years", comparison.bestValue().warranty());
    }
}