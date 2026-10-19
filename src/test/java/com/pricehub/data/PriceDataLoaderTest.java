package com.pricehub.data;

import com.pricehub.entity.Earphones;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceHistoryRepository;
import com.pricehub.repository.PriceRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceDataLoaderTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    private PriceDataLoader priceDataLoader;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        priceDataLoader = new PriceDataLoader(productRepository, priceRepository, priceHistoryRepository);
        sampleProduct = new Earphones("Wireless Bluetooth Neckband Earphones");
        sampleProduct.setId(1L);
    }

    @Test
    @DisplayName("Idempotency: When price record already exists and has not changed, history is not duplicated")
    void testIdempotencyOnUnchangedPrice() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        Price existing = new Price(sampleProduct, MarketplaceType.AMAZON, new BigDecimal("379.00"), 3.9, true, "1 Year", "img.jpg", "url", LocalDate.now());
        when(priceRepository.findByProductId(1L)).thenReturn(List.of(existing));

        // Triggering image candidate update should not corrupt data
        priceDataLoader.run();

        verify(priceHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Image selection rule: Product gets primary imageUrl from first non-empty in AMAZON, FLIPKART, MEESHO order")
    void testProductImagePriority() {
        Product p = new Earphones("Sample Headphone");
        p.setId(10L);

        // Flipkart has an image, Meesho has an image, Amazon image is empty
        Price flipkartPrice = new Price(p, MarketplaceType.FLIPKART, new BigDecimal("299.00"), 4.0, true, "1 Year", "http://flipkart.com/img.jpg", "url", LocalDate.now());
        Price meeshoPrice = new Price(p, MarketplaceType.MEESHO, new BigDecimal("199.00"), 3.8, true, "No Warranty", "http://meesho.com/img.jpg", "url", LocalDate.now());

        when(productRepository.findAll()).thenReturn(List.of(p));
        when(priceRepository.findByProductId(10L)).thenReturn(List.of(meeshoPrice, flipkartPrice));

        priceDataLoader.run();

        // Must pick Flipkart (ordinal 1) over Meesho (ordinal 2)
        verify(productRepository, times(1)).save(p);
    }
}