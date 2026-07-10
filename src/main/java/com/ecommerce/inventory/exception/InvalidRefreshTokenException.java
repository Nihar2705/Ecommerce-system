package com.ecommerce.inventory.exception;

/**
 * Thrown when a refresh token supplied to POST /api/auth/refresh-token
 * does not exist in the database (i.e. it is invalid/unknown).
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
