package com.pricehub.exception;

import java.time.LocalDateTime;

/**
 * OOP: Record - Immutable data carrier for standardized API error responses.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now());
    }
}