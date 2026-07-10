package com.ecommerce.inventory.exception;

/**
 * Thrown when a refresh token exists in the database but has passed its expiry date.
 */
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
