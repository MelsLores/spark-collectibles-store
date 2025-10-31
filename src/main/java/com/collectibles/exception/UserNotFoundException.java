package com.collectibles.exception;

/**
 * Exception thrown when a requested user is not found in the database
 * HTTP Status: 404 Not Found
 */
public class UserNotFoundException extends RuntimeException {
    private final String userId;

    public UserNotFoundException(String userId) {
        super(String.format("User not found with ID: %s", userId));
        this.userId = userId;
    }

    public UserNotFoundException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
