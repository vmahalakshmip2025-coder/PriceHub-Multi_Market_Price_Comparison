package com.pricehub.service;

import com.pricehub.dto.PriceUpdateRequest;
import com.pricehub.entity.Price;
import com.pricehub.entity.PriceHistory;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.exception.InvalidPriceException;
import com.pricehub.exception.ProductNotFoundException;
import com.pricehub.repository.PriceHistoryRepository;
import com.pricehub.repository.PriceRepository;
import com.pricehub.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class PriceUpdateService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // OOP: Constructor Injection
    public PriceUpdateService(ProductRepository productRepository,
                              PriceRepository priceRepository,
                              PriceHistoryRepository priceHistoryRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public Price updatePrice(Long productId, MarketplaceType marketplace, PriceUpdateRequest request) {
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Price cannot be negative: " + request.price());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Price price = priceRepository.findByProductIdAndMarketplace(productId, marketplace)
                .orElseGet(() -> new Price(
                        product,
                        marketplace,
                        request.price(),
                        request.rating(),
                        request.inStock() != null ? request.inStock() : true,
                        request.warranty() != null ? request.warranty() : "Warranty not listed",
                        request.imageUrl(),
                        request.productUrl(),
                        LocalDate.now()
                ));

        if (price.getId() != null) {
            // Existing price: apply mutations
            if (request.price() != null) price.setPrice(request.price());
            if (request.rating() != null) price.setRating(request.rating());
            if (request.inStock() != null) price.setInStock(request.inStock());
            if (request.warranty() != null) price.setWarranty(request.warranty());
            if (request.imageUrl() != null) price.setImageUrl(request.imageUrl());
            if (request.productUrl() != null) price.setProductUrl(request.productUrl());
            price.setPriceDate(LocalDate.now());
        }

        Price saved = priceRepository.save(price);

        // Record history snapshot on manual update
        PriceHistory history = new PriceHistory(
                product,
                marketplace,
                saved.getPrice(),
                saved.isInStock(),
                saved.getRating(),
                LocalDateTime.now()
        );
        priceHistoryRepository.save(history);

        return saved;
    }
}