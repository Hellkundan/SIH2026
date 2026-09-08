package com.dixy.backend.exception;

/**
 * Thrown when a requested resource (User, Tender, Bidder, Document) does not exist.
 * Results in HTTP 404 Not Found.
 *
 * Usage example:
 *   throw new ResourceNotFoundException("Tender not found with id: " + tenderId);
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
