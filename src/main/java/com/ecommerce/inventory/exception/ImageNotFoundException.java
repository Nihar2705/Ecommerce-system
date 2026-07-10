package com.ecommerce.inventory.exception;

/**
 * Thrown when a product has no image, or its referenced image file cannot be found
 * on disk (e.g. GET /api/products/{id}/image for a product without an uploaded image).
 */
public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(String message) {
        super(message);
    }
}
