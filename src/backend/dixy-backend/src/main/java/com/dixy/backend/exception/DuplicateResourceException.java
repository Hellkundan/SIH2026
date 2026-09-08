package com.dixy.backend.exception;

/**
 * Thrown when trying to create a resource that already exists.
 * Examples: registering a username that already exists, duplicate bidder ID.
 * Results in HTTP 409 Conflict.
 *
 * Usage example:
 *   throw new DuplicateResourceException("Username 'admin' is already taken");
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
