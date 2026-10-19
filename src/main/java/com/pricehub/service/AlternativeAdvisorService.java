package com.pricehub.service;

import com.pricehub.dto.AlternativeRecommendationDTO;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.exception.ProductNotFoundException;
import com.pricehub.repository.PriceRepository;
import com.pricehub.repository.ProductRepository;
import com.pricehub.strategy.ValueScoringStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * OOP: Service Layer utilizing the Strategy Pattern for recommending alternative products.
 */
@Service
@Transactional(readOnly = true)
public class AlternativeAdvisorService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ValueScoringStrategy scoringStrategy;

    // OOP: Constructor Injection of Strategy implementation
    public AlternativeAdvisorService(ProductRepository productRepository,
                                     PriceRepository priceRepository,
                                     ValueScoringStrategy generalScoringStrategy) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.scoringStrategy = generalScoringStrategy;
    }

    /**
     * Alternative Advisor Heuristics:
     * - Same category only, never the searched product.
     * - Uses each product's lowest in-stock price.
     * - Candidates must be within +/- 50% price of searched product.
     * - Scored via ValueScoringStrategy (price 60% + rating 40%; no rating -> price only).
     * - Keep items scoring >= 95% of target product's score.
     * - Return top 3 with readable reason and cheapest marketplace.
     * - If none qualify, returns category peers as fallback options.
     */
    public List<AlternativeRecommendationDTO> getAlternatives(Long productId) {
        Product targetProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Lowest in-stock price for the target product
        Price targetBestPrice = getLowestInStockPrice(targetProduct.getId());
        BigDecimal benchmarkPrice = (targetBestPrice != null) ? targetBestPrice.getPrice() : null;
        Double benchmarkRating = (targetBestPrice != null) ? targetBestPrice.getRating() : null;

        // Benchmark score of the target product itself
        double benchmarkScore = (benchmarkPrice != null)
                ? scoringStrategy.calculateScore(benchmarkPrice, benchmarkPrice, benchmarkRating)
                : 50.0;

        List<Product> categoryProducts = productRepository.findByCategory(targetProduct.getCategory());
        List<AlternativeRecommendationDTO> candidates = new ArrayList<>();

        for (Product peer : categoryProducts) {
            // Never recommend the searched product itself
            if (peer.getId().equals(targetProduct.getId())) {
                continue;
            }

            Price peerLowestPrice = getLowestInStockPrice(peer.getId());
            if (peerLowestPrice == null || peerLowestPrice.getPrice() == null) {
                continue; // Cannot evaluate without price
            }

            BigDecimal price = peerLowestPrice.getPrice();

            // Candidate price must be within +/- 50% of target price
            if (benchmarkPrice != null) {
                BigDecimal minBound = benchmarkPrice.multiply(BigDecimal.valueOf(0.50));
                BigDecimal maxBound = benchmarkPrice.multiply(BigDecimal.valueOf(1.50));

                if (price.compareTo(minBound) < 0 || price.compareTo(maxBound) > 0) {
                    continue;
                }
            }

            double peerScore = scoringStrategy.calculateScore(price, benchmarkPrice != null ? benchmarkPrice : price, peerLowestPrice.getRating());

            // Must score higher than or within 5% of searched product (peerScore >= benchmarkScore * 0.95)
            if (peerScore >= (benchmarkScore * 0.95)) {
                String reason = buildExplanation(price, benchmarkPrice, peerLowestPrice.getRating(), benchmarkRating, peerLowestPrice.getMarketplace().getDisplayName());

                candidates.add(new AlternativeRecommendationDTO(
                        peer.getId(),
                        peer.getName(),
                        peer.getCategory(),
                        peer.getImageUrl(),
                        peerLowestPrice.getMarketplace().getDisplayName(),
                        price,
                        peerLowestPrice.getRating(),
                        Math.round(peerScore * 10.0) / 10.0,
                        reason,
                        false
                ));
            }
        }

        // Sort by score descending and take top 3
        candidates.sort(Comparator.comparingDouble(AlternativeRecommendationDTO::score).reversed());
        if (!candidates.isEmpty()) {
            return candidates.stream().limit(3).toList();
        }

        // Fallback: If no candidate qualifies, return other products in this category
        List<AlternativeRecommendationDTO> fallbacks = new ArrayList<>();
        for (Product peer : categoryProducts) {
            if (peer.getId().equals(targetProduct.getId())) {
                continue;
            }
            Price peerLowestPrice = getLowestInStockPrice(peer.getId());
            BigDecimal price = peerLowestPrice != null ? peerLowestPrice.getPrice() : null;
            Double rating = peerLowestPrice != null ? peerLowestPrice.getRating() : null;
            String marketplace = peerLowestPrice != null ? peerLowestPrice.getMarketplace().getDisplayName() : "N/A";

            fallbacks.add(new AlternativeRecommendationDTO(
                    peer.getId(),
                    peer.getName(),
                    peer.getCategory(),
                    peer.getImageUrl(),
                    marketplace,
                    price,
                    rating,
                    0.0,
                    "Other option in this category",
                    true
            ));
        }

        return fallbacks.stream().limit(3).toList();
    }

    private Price getLowestInStockPrice(Long productId) {
        List<Price> prices = priceRepository.findByProductId(productId);
        Price lowest = null;
        for (Price p : prices) {
            if (p.isInStock() && p.getPrice() != null) {
                if (lowest == null || p.getPrice().compareTo(lowest.getPrice()) < 0) {
                    lowest = p;
                }
            }
        }
        return lowest;
    }

    private String buildExplanation(BigDecimal altPrice, BigDecimal targetPrice, Double altRating, Double targetRating, String marketplace) {
        if (targetPrice == null) {
            return "Popular value alternative available on " + marketplace;
        }

        int priceComparison = altPrice.compareTo(targetPrice);
        if (priceComparison < 0) {
            BigDecimal savings = targetPrice.subtract(altPrice);
            return String.format("Save ₹%s compared to searched product, available on %s.", savings, marketplace);
        } else if (altRating != null && (targetRating == null || altRating > targetRating)) {
            return String.format("Higher rated choice (%.1f★) at comparable price on %s.", altRating, marketplace);
        } else {
            return String.format("Competitive alternative offering strong value on %s.", marketplace);
        }
    }
}