package com.pricehub.entity;

import com.pricehub.entity.enums.MarketplaceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * OOP: Encapsulation with Data Validation.
 * Represents the current recorded price entry for a product on a specific marketplace.
 */
@Entity
@Table(
    name = "prices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "marketplace"})
)
public class Price extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false, length = 30)
    private MarketplaceType marketplace;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock = true;

    @Column(name = "warranty", length = 100)
    private String warranty = "Warranty not listed";

    @Column(name = "image_url", length = 700)
    private String imageUrl;

    @Column(name = "product_url", length = 1000)
    private String productUrl;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    public Price() {
        super();
    }

    // OOP: Method Overloading - Constructor
    public Price(Product product, MarketplaceType marketplace, BigDecimal price, Double rating,
                 boolean inStock, String warranty, String imageUrl, String productUrl, LocalDate priceDate) {
        super();
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        this.marketplace = Objects.requireNonNull(marketplace, "Marketplace cannot be null");
        setPrice(price);
        setRating(rating);
        this.inStock = inStock;
        this.warranty = (warranty != null && !warranty.isBlank()) ? warranty.trim() : "Warranty not listed";
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.priceDate = (priceDate != null) ? priceDate : LocalDate.now();
    }

    // OOP: Encapsulation with Data Validation
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
    }

    public MarketplaceType getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(MarketplaceType marketplace) {
        this.marketplace = Objects.requireNonNull(marketplace, "Marketplace cannot be null");
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        if (rating != null && (rating < 0.0 || rating > 5.0)) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.rating = rating;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = (warranty != null && !warranty.isBlank()) ? warranty.trim() : "Warranty not listed";
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = (priceDate != null) ? priceDate : LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price other = (Price) o;
        return Objects.equals(product != null ? product.getId() : null, other.product != null ? other.product.getId() : null)
                && marketplace == other.marketplace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(product != null ? product.getId() : null, marketplace);
    }

    @Override
    public String toString() {
        return "Price{" +
                "marketplace=" + marketplace +
                ", price=" + price +
                ", rating=" + rating +
                ", inStock=" + inStock +
                ", priceDate=" + priceDate +
                '}';
    }
}