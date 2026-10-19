package com.pricehub.exception;

/**
 * OOP: Custom Exception hierarchy - Checked/Unchecked domain exception.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }
}