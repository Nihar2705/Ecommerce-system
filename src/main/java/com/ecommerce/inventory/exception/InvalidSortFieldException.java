package com.ecommerce.inventory.exception;

/**
 * Thrown when a client requests sorting by a field that doesn't exist / isn't sortable
 * on the target entity (e.g. ?sortBy=nonExistentField).
 */
public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String message) {
        super(message);
    }
}
