package com.pricehub.validation;

import com.pricehub.entity.Earphones;
import com.pricehub.entity.Price;
import com.pricehub.entity.Product;
import com.pricehub.entity.enums.MarketplaceType;
import com.pricehub.exception.ErrorResponse;
import com.pricehub.exception.GlobalExceptionHandler;
import com.pricehub.exception.InvalidPriceException;
import com.pricehub.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationAndExceptionTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Product entity validation: Blank name throws IllegalArgumentException")
    void testProductNameValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Earphones("   "));
        assertThrows(IllegalArgumentException.class, () -> new Earphones(null));
    }

    @Test
    @DisplayName("Price entity validation: Negative price throws IllegalArgumentException")
    void testPriceNegativeValidation() {
        Product product = new Earphones("Valid Earphone");
        assertThrows(IllegalArgumentException.class, () ->
                new Price(product, MarketplaceType.AMAZON, new BigDecimal("-10.00"), 4.0, true, "1 Year", null, null, LocalDate.now())
        );
    }

    @Test
    @DisplayName("Price entity validation: Out-of-bounds rating throws IllegalArgumentException")
    void testPriceRatingBoundsValidation() {
        Product product = new Earphones("Valid Earphone");
        assertThrows(IllegalArgumentException.class, () ->
                new Price(product, MarketplaceType.AMAZON, new BigDecimal("100.00"), 5.5, true, "1 Year", null, null, LocalDate.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Price(product, MarketplaceType.AMAZON, new BigDecimal("100.00"), -0.1, true, "1 Year", null, null, LocalDate.now())
        );
    }

    @Test
    @DisplayName("GlobalExceptionHandler: ProductNotFoundException returns 404 NOT_FOUND")
    void testHandleProductNotFoundException() {
        ProductNotFoundException ex = new ProductNotFoundException(99L);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleProductNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Product not found with id: 99", response.getBody().message());
    }

    @Test
    @DisplayName("GlobalExceptionHandler: InvalidPriceException returns 400 BAD_REQUEST")
    void testHandleInvalidPriceException() {
        InvalidPriceException ex = new InvalidPriceException("Price cannot be negative: -50");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidPrice(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Price cannot be negative: -50", response.getBody().message());
    }
}