package com.ecommerce.inventory.exception;

/**
 * Thrown when an uploaded product image exceeds the maximum allowed size (5 MB).
 */
public class FileSizeExceededException extends RuntimeException {

    public FileSizeExceededException(String message) {
        super(message);
    }
}
