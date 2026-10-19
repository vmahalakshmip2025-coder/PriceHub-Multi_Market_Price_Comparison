package com.pricehub.entity;

import com.pricehub.entity.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Inheritance - Specialization of Product for home decor.
 */
@Entity
@DiscriminatorValue("HOME_DECOR")
public class HomeDecorItem extends Product {

    @Column(name = "material")
    private String material;

    @Column(name = "item_type")
    private String itemType;

    public HomeDecorItem() {
        super();
        setCategory(ProductCategory.HOME_AND_KIDS);
    }

    // OOP: Method Overloading - Constructor 1
    public HomeDecorItem(String name) {
        super(name, ProductCategory.HOME_AND_KIDS);
    }

    // OOP: Method Overloading - Constructor 2
    public HomeDecorItem(String name, String material, String itemType) {
        super(name, ProductCategory.HOME_AND_KIDS);
        this.material = material;
        this.itemType = itemType;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}