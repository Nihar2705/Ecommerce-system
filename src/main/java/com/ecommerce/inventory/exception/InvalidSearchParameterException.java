package com.ecommerce.inventory.exception;

/**
 * Thrown by the product search endpoint when zero, or more than one, group of search
 * criteria (keyword / category / price range) is supplied in the same request.
 * Each call to GET /api/products/search must use exactly one search mode.
 */
public class InvalidSearchParameterException extends RuntimeException {

    public InvalidSearchParameterException(String message) {
        super(message);
    }
}
