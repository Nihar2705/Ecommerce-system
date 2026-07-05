package com.ecommerce.inventory.exception;

/**
 * Thrown during registration when the username or email is already taken.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
