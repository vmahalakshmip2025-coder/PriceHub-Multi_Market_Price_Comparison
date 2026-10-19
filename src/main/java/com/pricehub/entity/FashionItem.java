package com.pricehub.entity;

import com.pricehub.entity.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Inheritance - Base class for fashion goods and apparel.
 */
@Entity
@DiscriminatorValue("FASHION")
public class FashionItem extends Product {

    @Column(name = "size_variant")
    private String sizeVariant;

    @Column(name = "gender_target")
    private String genderTarget;

    public FashionItem() {
        super();
        setCategory(ProductCategory.FASHION);
    }

    // OOP: Method Overloading - Constructor 1
    public FashionItem(String name) {
        super(name, ProductCategory.FASHION);
        this.sizeVariant = "Free Size";
    }

    // OOP: Method Overloading - Constructor 2
    public FashionItem(String name, String sizeVariant, String genderTarget) {
        super(name, ProductCategory.FASHION);
        this.sizeVariant = sizeVariant;
        this.genderTarget = genderTarget;
    }

    public String getSizeVariant() {
        return sizeVariant;
    }

    public void setSizeVariant(String sizeVariant) {
        this.sizeVariant = sizeVariant;
    }

    public String getGenderTarget() {
        return genderTarget;
    }

    public void setGenderTarget(String genderTarget) {
        this.genderTarget = genderTarget;
    }
}