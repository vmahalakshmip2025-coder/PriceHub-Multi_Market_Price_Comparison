package com.pricehub.data;

import com.pricehub.entity.Backpack;
import com.pricehub.entity.Dress;
import com.pricehub.entity.Earphones;
import com.pricehub.entity.FashionItem;
import com.pricehub.entity.HomeDecorItem;
import com.pricehub.entity.KidsCycle;
import com.pricehub.entity.Product;
import com.pricehub.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * OOP: Polymorphic object construction.
 * Seeds the exact 12 products required by PriceHub if not already present in the database.
 * Does NOT set or hardcode any prices or ratings (strictly adheres to CSV requirement).
 */
@Component
@Order(1) // Runs first to ensure products exist prior to loading prices
public class ProductSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductSeeder.class);

    private final ProductRepository productRepository;

    // OOP: Constructor Injection
    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing ProductSeeder: Checking baseline catalog...");

        List<Product> catalog = new ArrayList<>();

        // 1-3. Earphones (Audio category)
        catalog.add(new Earphones("Wireless Bluetooth Neckband Earphones", true, true));
        catalog.add(new Earphones("True Wireless Earbuds with Charging Case", true, true));
        catalog.add(new Earphones("Wired In-Ear Earphones with Mic", false, true));

        // 4-7. Dresses (Fashion category, extends FashionItem)
        catalog.add(new Dress("Women's Cotton Churidar Set", "Churidar", "Pure Cotton"));
        catalog.add(new Dress("Girls Cotton Party Frock", "Party Frock", "Cotton Blend"));
        catalog.add(new Dress("Women's Anarkali Kurti Set", "Anarkali", "Rayon Cotton"));
        catalog.add(new Dress("Women's Floral Print Maxi Dress", "Maxi Dress", "Georgette Cotton"));

        // 8-9. Fashion Items (Fashion category)
        catalog.add(new FashionItem("Men's Running Sports Shoes", "UK 8-11", "Men"));

        // 10. Backpack class used as a bag (Fashion category)
        catalog.add(new Backpack("Women's Sling Bag", 5, true));

        // 11. Home Decor Item (Home and Kids category)
        catalog.add(new HomeDecorItem("Home Decor Item", "Ceramic / Wood", "Showpiece"));

        // 12. Kids Cycle (Home and Kids category)
        catalog.add(new KidsCycle("Kids Cycle", 14, "3-6 Years", "Bright Red"));

        int seededCount = 0;
        for (Product product : catalog) {
            // Idempotent seeding check
            if (productRepository.findByNameIgnoreCase(product.getName()).isEmpty()) {
                productRepository.save(product);
                seededCount++;
            }
        }

        logger.info("ProductSeeder finished: {} new products saved. Total catalog size: {}",
                seededCount, productRepository.count());
    }
}