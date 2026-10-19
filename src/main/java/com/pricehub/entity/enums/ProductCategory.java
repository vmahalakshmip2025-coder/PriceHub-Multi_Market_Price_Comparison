package com.pricehub.entity.enums;

/**
 * OOP: Classification using strongly-typed Enums to avoid magic strings.
 */
public enum ProductCategory {
    EARPHONES("Earphones & Audio"),
    FASHION("Fashion & Apparel"),
    HOME_AND_KIDS("Home Decor & Kids");

    private final String displayCategory;

    ProductCategory(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }
}