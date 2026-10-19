package com.pricehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Inheritance - Specialization of FashionItem for bags/backpacks.
 */
@Entity
@DiscriminatorValue("BACKPACK")
public class Backpack extends FashionItem {

    @Column(name = "capacity_liters")
    private Integer capacityLiters;

    @Column(name = "is_waterproof")
    private Boolean waterproof;

    public Backpack() {
        super();
    }

    // OOP: Method Overloading - Constructor 1
    public Backpack(String name) {
        super(name);
        this.waterproof = false;
    }

    // OOP: Method Overloading - Constructor 2
    public Backpack(String name, Integer capacityLiters, Boolean waterproof) {
        super(name);
        this.capacityLiters = capacityLiters;
        this.waterproof = waterproof;
    }

    public Integer getCapacityLiters() {
        return capacityLiters;
    }

    public void setCapacityLiters(Integer capacityLiters) {
        this.capacityLiters = capacityLiters;
    }

    public Boolean getWaterproof() {
        return waterproof;
    }

    public void setWaterproof(Boolean waterproof) {
        this.waterproof = waterproof;
    }
}