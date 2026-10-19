package com.pricehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Multi-level Inheritance - Dress extends FashionItem which extends Product.
 */
@Entity
@DiscriminatorValue("DRESS")
public class Dress extends FashionItem {

    @Column(name = "dress_style")
    private String dressStyle;

    @Column(name = "fabric")
    private String fabric;

    public Dress() {
        super();
    }

    // OOP: Method Overloading - Constructor 1
    public Dress(String name) {
        super(name);
        this.fabric = "Cotton";
    }

    // OOP: Method Overloading - Constructor 2
    public Dress(String name, String dressStyle, String fabric) {
        super(name);
        this.dressStyle = dressStyle;
        this.fabric = fabric;
    }

    public String getDressStyle() {
        return dressStyle;
    }

    public void setDressStyle(String dressStyle) {
        this.dressStyle = dressStyle;
    }

    public String getFabric() {
        return fabric;
    }

    public void setFabric(String fabric) {
        this.fabric = fabric;
    }
}