package com.ecommerce.inventory.exception;

/**
 * Thrown when a price-range search request has minPrice greater than maxPrice.
 */
public class InvalidPriceRangeException extends RuntimeException {

    public InvalidPriceRangeException(String message) {
        super(message);
    }
}
