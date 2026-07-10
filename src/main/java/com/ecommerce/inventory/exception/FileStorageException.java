package com.ecommerce.inventory.exception;

/**
 * Thrown when a local file system operation (writing, reading, or deleting a
 * product image) fails unexpectedly, e.g. due to an I/O error.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
