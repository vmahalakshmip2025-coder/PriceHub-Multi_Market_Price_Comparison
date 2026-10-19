package com.pricehub.entity;

import com.pricehub.entity.enums.ProductCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OOP: Inheritance & Polymorphism.
 * Uses JPA SINGLE_TABLE inheritance with a discriminator column.
 * Abstract class provides reusable fields and behavior to subclasses.
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Product extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand = "Generic";

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProductCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Price> prices = new ArrayList<>();

    // Protected zero-argument constructor for JPA
    protected Product() {
        super();
    }

    // OOP: Method Overloading - Constructor 1
    protected Product(String name, ProductCategory category) {
        this(name, "Generic", category, null);
    }

    // OOP: Method Overloading - Constructor 2
    protected Product(String name, String brand, ProductCategory category, String description) {
        super();
        setName(name);
        setBrand(brand);
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.description = description;
    }

    // OOP: Method Overloading - Behavior customization
    public void updateDetails(String name) {
        setName(name);
    }

    public void updateDetails(String name, String description) {
        setName(name);
        this.description = description;
    }

    // OOP: Encapsulation with Data Validation
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            this.brand = "Generic";
        } else {
            this.brand = brand.trim();
        }
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    public void addPrice(Price price) {
        prices.add(price);
        price.setProduct(this);
    }

    public void removePrice(Price price) {
        prices.remove(price);
        price.setProduct(null);
    }

    // OOP: Overriding standard Object methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", category=" + category +
                '}';
    }
}