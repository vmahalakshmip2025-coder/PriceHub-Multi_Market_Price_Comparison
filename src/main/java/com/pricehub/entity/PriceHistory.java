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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historical audit log capturing price variations over time.
 */
@Entity
@Table(name = "price_history")
public class PriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false, length = 30)
    private MarketplaceType marketplace;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public PriceHistory() {
        super();
    }

    // OOP: Method Overloading - Constructor
    public PriceHistory(Product product, MarketplaceType marketplace, BigDecimal price,
                        boolean inStock, Double rating, LocalDateTime recordedAt) {
        super();
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        this.marketplace = Objects.requireNonNull(marketplace, "Marketplace cannot be null");
        this.price = price;
        this.inStock = inStock;
        this.rating = rating;
        this.recordedAt = (recordedAt != null) ? recordedAt : LocalDateTime.now();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public MarketplaceType getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(MarketplaceType marketplace) {
        this.marketplace = marketplace;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public String toString() {
        return "PriceHistory{" +
                "id=" + getId() +
                ", marketplace=" + marketplace +
                ", price=" + price +
                ", recordedAt=" + recordedAt +
                '}';
    }
}