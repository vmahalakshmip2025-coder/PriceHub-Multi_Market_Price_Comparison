package com.pricehub.entity;

import com.pricehub.entity.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * OOP: Inheritance - Specialization of Product for kids cycles and toys.
 */
@Entity
@DiscriminatorValue("KIDS_CYCLE")
public class KidsCycle extends Product {

    @Column(name = "wheel_size_inches")
    private Integer wheelSizeInches;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "color")
    private String color;

    public KidsCycle() {
        super();
        setCategory(ProductCategory.HOME_AND_KIDS);
    }

    // OOP: Method Overloading - Constructor 1
    public KidsCycle(String name) {
        super(name, ProductCategory.HOME_AND_KIDS);
    }

    // OOP: Method Overloading - Constructor 2
    public KidsCycle(String name, Integer wheelSizeInches, String ageGroup, String color) {
        super(name, ProductCategory.HOME_AND_KIDS);
        this.wheelSizeInches = wheelSizeInches;
        this.ageGroup = ageGroup;
        this.color = color;
    }

    public Integer getWheelSizeInches() {
        return wheelSizeInches;
    }

    public void setWheelSizeInches(Integer wheelSizeInches) {
        this.wheelSizeInches = wheelSizeInches;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}