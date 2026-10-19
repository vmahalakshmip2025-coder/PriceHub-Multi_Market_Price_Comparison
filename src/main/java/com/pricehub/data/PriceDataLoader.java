package com.pricehub.data;

import com.pricehub.entity.Price;
import com.pricehub.entity.PriceHistory;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.repository.PriceHistoryRepository;
import com.pricehub.repository.PriceRepository;
import com.pricehub.repository.ProductRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Apache Commons CSV-based data ingestion engine.
 * Idempotent, robust BOM handling, strict data validation, and price history auditing.
 */
@Component
@Order(2) // Runs after ProductSeeder
public class PriceDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PriceDataLoader.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // OOP: Constructor Injection
    public PriceDataLoader(ProductRepository productRepository,
                           PriceRepository priceRepository,
                           PriceHistoryRepository priceHistoryRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        loadPricesFromCsv();
        updateProductImageCandidates();
    }

    /**
     * Reads src/main/resources/data/prices.csv and loads prices idempotently.
     */
    public void loadPricesFromCsv() {
        ClassPathResource resource = new ClassPathResource("data/prices.csv");
        if (!resource.exists()) {
            logger.warn("prices.csv not found at data/prices.csv. Skipping CSV ingestion.");
            return;
        }

        int rowsRead = 0;
        int rowsLoaded = 0;
        int rowsSkipped = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build();

            try (CSVParser parser = csvFormat.parse(reader)) {
                for (CSVRecord record : parser) {
                    rowsRead++;
                    long rowNum = record.getRecordNumber();

                    // 1. Required field: productName
                    String productName = getRecordValue(record, "productName");
                    if (productName == null || productName.isBlank()) {
                        logger.warn("Row {}: Skipped - missing required productName", rowNum);
                        rowsSkipped++;
                        continue;
                    }

                    // 2. Resolve Product by Name
                    Optional<Product> optionalProduct = productRepository.findByNameIgnoreCase(productName.trim());
                    if (optionalProduct.isEmpty()) {
                        logger.warn("Row {}: Skipped - unknown productName '{}'", rowNum, productName);
                        rowsSkipped++;
                        continue;
                    }
                    Product product = optionalProduct.get();

                    // 3. Required field: marketplace
                    String marketplaceRaw = getRecordValue(record, "marketplace");
                    MarketplaceType marketplace = MarketplaceType.fromString(marketplaceRaw);
                    if (marketplace == null) {
                        logger.warn("Row {}: Skipped - unknown or missing marketplace '{}'", rowNum, marketplaceRaw);
                        rowsSkipped++;
                        continue;
                    }

                    // 4. Parse price (empty price = "Not available" for that marketplace)
                    String priceRaw = getRecordValue(record, "price");
                    BigDecimal price = null;
                    if (priceRaw != null && !priceRaw.isBlank()) {
                        try {
                            price = new BigDecimal(priceRaw.trim());
                            if (price.compareTo(BigDecimal.ZERO) < 0) {
                                logger.warn("Row {}: Skipped - negative price '{}'", rowNum, priceRaw);
                                rowsSkipped++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Row {}: Skipped - unparseable price '{}'", rowNum, priceRaw);
                            rowsSkipped++;
                            continue;
                        }
                    }

                    // 5. Parse rating (0.0 to 5.0; outside = skip row with warning)
                    String ratingRaw = getRecordValue(record, "rating");
                    Double rating = null;
                    if (ratingRaw != null && !ratingRaw.isBlank()) {
                        try {
                            double parsedRating = Double.parseDouble(ratingRaw.trim());
                            if (parsedRating < 0.0 || parsedRating > 5.0) {
                                logger.warn("Row {}: Skipped - rating {} is out of bounds [0.0 - 5.0]", rowNum, parsedRating);
                                rowsSkipped++;
                                continue;
                            }
                            rating = parsedRating;
                        } catch (NumberFormatException e) {
                            logger.warn("Row {}: Skipped - invalid rating format '{}'", rowNum, ratingRaw);
                            rowsSkipped++;
                            continue;
                        }
                    }

                    // 6. Parse inStock: TRUE/true/1/yes = true, FALSE/false/0/no = false, empty = true
                    String inStockRaw = getRecordValue(record, "inStock");
                    boolean inStock = parseBooleanWithDefaultTrue(inStockRaw);

                    // 7. Parse warranty: free text, empty = "Warranty not listed"
                    String warrantyRaw = getRecordValue(record, "warranty");
                    String warranty = (warrantyRaw != null && !warrantyRaw.isBlank())
                            ? warrantyRaw.trim()
                            : "Warranty not listed";

                    // 8. URLs (optional)
                    String imageUrl = getRecordValue(record, "imageUrl");
                    String productUrl = getRecordValue(record, "productUrl");

                    // 9. Parse priceDate: yyyy-MM-dd, empty = today
                    String priceDateRaw = getRecordValue(record, "priceDate");
                    LocalDate priceDate = LocalDate.now();
                    if (priceDateRaw != null && !priceDateRaw.isBlank()) {
                        try {
                            priceDate = LocalDate.parse(priceDateRaw.trim(), DATE_FORMATTER);
                        } catch (DateTimeParseException e) {
                            logger.warn("Row {}: Invalid priceDate '{}', defaulting to today", rowNum, priceDateRaw);
                            priceDate = LocalDate.now();
                        }
                    }

                    // 10. Persist or Update Price entity & audit PriceHistory
                    saveOrUpdatePrice(product, marketplace, price, rating, inStock, warranty, imageUrl, productUrl, priceDate);
                    rowsLoaded++;
                }
            }

            logger.info("PriceDataLoader ingestion finished: rows read: {} / loaded: {} / skipped: {}",
                    rowsRead, rowsLoaded, rowsSkipped);

        } catch (Exception e) {
            logger.error("Failed to parse prices.csv: {}", e.getMessage(), e);
        }
    }

    private void saveOrUpdatePrice(Product product, MarketplaceType marketplace, BigDecimal price,
                                   Double rating, boolean inStock, String warranty, String imageUrl,
                                   String productUrl, LocalDate priceDate) {

        Optional<Price> existingPriceOpt = priceRepository.findByProductIdAndMarketplace(product.getId(), marketplace);

        if (existingPriceOpt.isEmpty()) {
            // First time load: create Price record and initial PriceHistory
            Price newPrice = new Price(product, marketplace, price, rating, inStock, warranty, imageUrl, productUrl, priceDate);
            priceRepository.save(newPrice);

            PriceHistory history = new PriceHistory(product, marketplace, price, inStock, rating, LocalDateTime.now());
            priceHistoryRepository.save(history);
        } else {
            Price currentPrice = existingPriceOpt.get();

            boolean priceChanged = (price != null && !price.equals(currentPrice.getPrice()))
                    || (price == null && currentPrice.getPrice() != null);
            boolean stockChanged = inStock != currentPrice.isInStock();
            boolean ratingChanged = (rating != null && !rating.equals(currentPrice.getRating()))
                    || (rating == null && currentPrice.getRating() != null);

            // Update attributes
            currentPrice.setPrice(price);
            currentPrice.setRating(rating);
            currentPrice.setInStock(inStock);
            currentPrice.setWarranty(warranty);
            currentPrice.setImageUrl(imageUrl);
            currentPrice.setProductUrl(productUrl);
            currentPrice.setPriceDate(priceDate);
            priceRepository.save(currentPrice);

            // Audit history if significant data changed
            if (priceChanged || stockChanged || ratingChanged) {
                PriceHistory history = new PriceHistory(product, marketplace, price, inStock, rating, LocalDateTime.now());
                priceHistoryRepository.save(history);
            }
        }
    }

    /**
     * Updates each product's primary imageUrl:
     * First non-empty imageUrl in the order: AMAZON, FLIPKART, MEESHO.
     */
    private void updateProductImageCandidates() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            List<Price> prices = priceRepository.findByProductId(product.getId());

            // Order: AMAZON, FLIPKART, MEESHO
            prices.sort(Comparator.comparingInt(p -> p.getMarketplace().ordinal()));

            for (Price p : prices) {
                if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                    product.setImageUrl(p.getImageUrl().trim());
                    productRepository.save(product);
                    break;
                }
            }
        }
    }

    private String getRecordValue(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return null;
        }
        String val = record.get(column);
        if (val == null) {
            return null;
        }
        // Strip UTF-8 BOM if present at start of token
        if (val.startsWith("\uFEFF")) {
            val = val.substring(1);
        }
        return val.trim();
    }

    private boolean parseBooleanWithDefaultTrue(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String clean = text.trim().toLowerCase();
        return clean.equals("true") || clean.equals("1") || clean.equals("yes");
    }
}