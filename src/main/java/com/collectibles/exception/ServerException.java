package com.collectibles.exception;

/**
 * Exception thrown when an unexpected server error occurs
 * HTTP Status: 500 Internal Server Error
 */
public class ServerException extends RuntimeException {
    private final String operation;

    public ServerException(String message) {
        super(message);
        this.operation = null;
    }

    public ServerException(String operation, String message) {
        super(message);
        this.operation = operation;
    }

    public ServerException(String operation, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
