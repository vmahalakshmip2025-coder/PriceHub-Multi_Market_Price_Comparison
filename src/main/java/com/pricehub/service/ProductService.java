package com.pricehub.service;

import com.pricehub.dto.ProductSummaryDTO;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.exception.ProductNotFoundException;
import com.pricehub.repository.PriceRepository;
import com.pricehub.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;

    // OOP: Constructor Injection
    public ProductService(ProductRepository productRepository, PriceRepository priceRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
    }

    public List<ProductSummaryDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductSummaryDTO> dtoList = new ArrayList<>();
        for (Product product : products) {
            dtoList.add(mapToSummaryDTO(product));
        }
        return dtoList;
    }

    public List<ProductSummaryDTO> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        List<Product> products = productRepository.searchByNameContainingIgnoreCase(query.trim());
        List<ProductSummaryDTO> dtoList = new ArrayList<>();
        for (Product product : products) {
            dtoList.add(mapToSummaryDTO(product));
        }
        return dtoList;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductSummaryDTO mapToSummaryDTO(Product product) {
        List<Price> prices = priceRepository.findByProductId(product.getId());

        BigDecimal minPrice = null;
        double ratingSum = 0.0;
        int ratingCount = 0;
        boolean inStockAnywhere = false;

        for (Price p : prices) {
            if (p.isInStock() && p.getPrice() != null) {
                inStockAnywhere = true;
                if (minPrice == null || p.getPrice().compareTo(minPrice) < 0) {
                    minPrice = p.getPrice();
                }
            }
            if (p.getRating() != null && p.getRating() > 0.0) {
                ratingSum += p.getRating();
                ratingCount++;
            }
        }

        Double avgRating = (ratingCount > 0) ? Math.round((ratingSum / ratingCount) * 10.0) / 10.0 : null;

        return new ProductSummaryDTO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getCategory().getDisplayCategory(),
                product.getImageUrl(),
                minPrice,
                avgRating,
                inStockAnywhere
        );
    }
}