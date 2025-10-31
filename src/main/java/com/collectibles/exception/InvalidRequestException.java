package com.collectibles.exception;

/**
 * Exception thrown when request validation fails
 * HTTP Status: 400 Bad Request
 */
public class InvalidRequestException extends RuntimeException {
    private final String field;

    public InvalidRequestException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
