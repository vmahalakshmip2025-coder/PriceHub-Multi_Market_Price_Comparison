package com.pricehub.service;

import com.pricehub.dto.BestValueDTO;
import com.pricehub.dto.MarketplacePriceDTO;
import com.pricehub.dto.PriceComparisonDTO;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.exception.ProductNotFoundException;
import com.pricehub.provider.MarketplacePriceProvider;
import com.pricehub.repository.ProductRepository;
import com.pricehub.util.WarrantyParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OOP: Service Layer implementing multi-marketplace comparison and Best Value heuristics.
 */
@Service
@Transactional(readOnly = true)
public class ComparisonService {

    private final ProductRepository productRepository;
    private final List<MarketplacePriceProvider> providers;

    // OOP: Constructor Injection of Spring-managed list of interface implementations
    public ComparisonService(ProductRepository productRepository, List<MarketplacePriceProvider> providers) {
        this.productRepository = productRepository;
        this.providers = providers;
    }

    public PriceComparisonDTO compareProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<MarketplacePriceDTO> marketplaceDtos = new ArrayList<>();
        List<Price> validInStockPrices = new ArrayList<>();

        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;
        Price cheapestPriceRecord = null;

        // Query each marketplace provider in consistent order: AMAZON, FLIPKART, MEESHO
        for (MarketplaceType type : MarketplaceType.values()) {
            MarketplacePriceProvider provider = findProvider(type);
            Optional<Price> priceOpt = provider.getPriceForProduct(product);

            if (priceOpt.isPresent()) {
                Price price = priceOpt.get();
                if (price.isInStock() && price.getPrice() != null) {
                    validInStockPrices.add(price);

                    if (lowestPrice == null || price.getPrice().compareTo(lowestPrice) < 0) {
                        lowestPrice = price.getPrice();
                        cheapestPriceRecord = price;
                    }
                    if (highestPrice == null || price.getPrice().compareTo(highestPrice) > 0) {
                        highestPrice = price.getPrice();
                    }
                }
            }
        }

        // Determine Best Value based on domain heuristic
        BestValueDTO bestValue = calculateBestValue(validInStockPrices, cheapestPriceRecord, highestPrice);

        // Build individual marketplace DTOs
        for (MarketplaceType type : MarketplaceType.values()) {
            MarketplacePriceProvider provider = findProvider(type);
            Optional<Price> priceOpt = provider.getPriceForProduct(product);

            if (priceOpt.isPresent()) {
                Price price = priceOpt.get();
                boolean isCheapest = cheapestPriceRecord != null && price.equals(cheapestPriceRecord);
                boolean isBest = bestValue != null && price.getMarketplace() == bestValue.marketplace();
                String formatted = (price.getPrice() != null) ? "₹" + price.getPrice() : "Not available";
                String targetUrl = provider.resolveProductUrl(product, price);

                marketplaceDtos.add(new MarketplacePriceDTO(
                        type,
                        type.getDisplayName(),
                        price.getPrice(),
                        formatted,
                        price.getRating(),
                        price.isInStock(),
                        price.getWarranty(),
                        price.getPriceDate(),
                        targetUrl,
                        price.getImageUrl(),
                        isCheapest,
                        isBest
                ));
            } else {
                // Record absent for marketplace
                marketplaceDtos.add(new MarketplacePriceDTO(
                        type,
                        type.getDisplayName(),
                        null,
                        "Not available",
                        null,
                        false,
                        "Warranty not listed",
                        null,
                        type.buildSearchUrl(product.getName()),
                        null,
                        false,
                        false
                ));
            }
        }

        BigDecimal savingsAmount = BigDecimal.ZERO;
        Double savingsPercentage = 0.0;
        if (lowestPrice != null && highestPrice != null && highestPrice.compareTo(lowestPrice) > 0) {
            savingsAmount = highestPrice.subtract(lowestPrice);
            savingsPercentage = savingsAmount.divide(highestPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        String cheapestMarketplaceName = (cheapestPriceRecord != null)
                ? cheapestPriceRecord.getMarketplace().getDisplayName()
                : "None";

        return new PriceComparisonDTO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getCategory().getDisplayCategory(),
                product.getImageUrl(),
                lowestPrice,
                highestPrice,
                savingsAmount,
                savingsPercentage,
                cheapestMarketplaceName,
                bestValue,
                marketplaceDtos
        );
    }

    /**
     * Best Value Logic:
     * Cheapest in-stock marketplace, UNLESS another in-stock one is within 5% of that price
     * AND rated at least 0.5 higher, then recommend that one.
     * Tie on price -> longer warranty (months). Always give a one-line reason.
     */
    private BestValueDTO calculateBestValue(List<Price> inStockPrices, Price initialCheapest, BigDecimal highestPrice) {
        if (inStockPrices.isEmpty() || initialCheapest == null) {
            return null;
        }

        // 1. Resolve ties on the lowest price using Warranty duration
        Price cheapest = initialCheapest;
        for (Price candidate : inStockPrices) {
            if (candidate.getPrice().compareTo(cheapest.getPrice()) == 0 && !candidate.equals(cheapest)) {
                int candMonths = WarrantyParser.parseToMonths(candidate.getWarranty());
                int currMonths = WarrantyParser.parseToMonths(cheapest.getWarranty());
                if (candMonths > currMonths) {
                    cheapest = candidate;
                }
            }
        }

        // 2. Check if another option is within 5% price AND rated >= 0.5 higher
        BigDecimal fivePercentThreshold = cheapest.getPrice().multiply(BigDecimal.valueOf(1.05));
        Price bestOption = cheapest;
        String reason;

        Price upgradedOption = null;
        for (Price candidate : inStockPrices) {
            if (!candidate.equals(cheapest)) {
                boolean withinFivePercent = candidate.getPrice().compareTo(fivePercentThreshold) <= 0;
                double cheapestRating = (cheapest.getRating() != null) ? cheapest.getRating() : 0.0;
                double candidateRating = (candidate.getRating() != null) ? candidate.getRating() : 0.0;
                boolean ratedSignificantlyHigher = (candidateRating - cheapestRating) >= 0.5;

                if (withinFivePercent && ratedSignificantlyHigher) {
                    upgradedOption = candidate;
                    break;
                }
            }
        }

        if (upgradedOption != null) {
            bestOption = upgradedOption;
            reason = String.format("Recommended over cheapest option: Only slightly higher price on %s (+₹%s) but offers a significantly better rating (%.1f vs %.1f).",
                    upgradedOption.getMarketplace().getDisplayName(),
                    upgradedOption.getPrice().subtract(cheapest.getPrice()),
                    upgradedOption.getRating(),
                    cheapest.getRating() != null ? cheapest.getRating() : 0.0);
        } else {
            int warrantyMonths = WarrantyParser.parseToMonths(bestOption.getWarranty());
            if (warrantyMonths > 0) {
                reason = String.format("Lowest in-stock price on %s at ₹%s with %s.",
                        bestOption.getMarketplace().getDisplayName(),
                        bestOption.getPrice(),
                        bestOption.getWarranty());
            } else {
                reason = String.format("Lowest available in-stock price on %s at ₹%s.",
                        bestOption.getMarketplace().getDisplayName(),
                        bestOption.getPrice());
            }
        }

        BigDecimal savingsAgainstHighest = BigDecimal.ZERO;
        if (highestPrice != null && highestPrice.compareTo(bestOption.getPrice()) > 0) {
            savingsAgainstHighest = highestPrice.subtract(bestOption.getPrice());
        }

        return new BestValueDTO(
                bestOption.getMarketplace(),
                bestOption.getMarketplace().getDisplayName(),
                bestOption.getPrice(),
                bestOption.getRating(),
                bestOption.getWarranty(),
                reason,
                savingsAgainstHighest
        );
    }

    private MarketplacePriceProvider findProvider(MarketplaceType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Provider not configured for marketplace: " + type));
    }
}