package com.pricehub.exception;

/**
 * OOP: Custom Exception thrown when price business rules are violated.
 */
public class InvalidPriceException extends RuntimeException {

    public InvalidPriceException(String message) {
        super(message);
    }
}