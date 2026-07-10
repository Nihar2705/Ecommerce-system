package com.ecommerce.inventory.exception;

/**
 * Thrown when an uploaded product image file is empty (zero bytes / no file selected).
 */
public class EmptyFileException extends RuntimeException {

    public EmptyFileException(String message) {
        super(message);
    }
}
