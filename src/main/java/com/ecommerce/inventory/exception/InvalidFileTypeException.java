package com.ecommerce.inventory.exception;

/**
 * Thrown when an uploaded product image is not a JPG, JPEG, or PNG file
 * (based on both its content type and file extension).
 */
public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
