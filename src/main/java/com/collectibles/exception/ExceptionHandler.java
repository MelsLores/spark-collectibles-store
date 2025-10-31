package com.collectibles.exception;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized Exception Handler for the Spark Collectibles Store API
 * 
 * This class provides standardized error handling across all endpoints.
 * It maps different exception types to appropriate HTTP status codes and
 * generates consistent error responses in JSON format.
 * 
 * Supported Exception Cases:
 * - 404 Not Found: ItemNotFoundException, UserNotFoundException
 * - 400 Bad Request: InvalidRequestException, validation errors
 * - 500 Internal Server Error: ServerException, unexpected errors
 */
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles ItemNotFoundException (404)
     * Returns a standardized error response when an item is not found
     */
    public static String handleItemNotFound(ItemNotFoundException ex, Request req, Response res) {
        logger.warn("Item not found: {} - Requested by: {}", ex.getItemId(), req.ip());
        
        res.status(404);
        res.type("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
            404,
            "Not Found",
            ex.getMessage(),
            req.pathInfo(),
            LocalDateTime.now().format(formatter)
        );
        
        return gson.toJson(errorResponse);
    }

    /**
     * Handles UserNotFoundException (404)
     * Returns a standardized error response when a user is not found
     */
    public static String handleUserNotFound(UserNotFoundException ex, Request req, Response res) {
        logger.warn("User not found: {} - Requested by: {}", ex.getUserId(), req.ip());
        
        res.status(404);
        res.type("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
            404,
            "Not Found",
            ex.getMessage(),
            req.pathInfo(),
            LocalDateTime.now().format(formatter)
        );
        
        return gson.toJson(errorResponse);
    }

    /**
     * Handles InvalidRequestException (400)
     * Returns a standardized error response for validation errors
     */
    public static String handleInvalidRequest(InvalidRequestException ex, Request req, Response res) {
        logger.warn("Invalid request: {} - Path: {} - IP: {}", 
            ex.getMessage(), req.pathInfo(), req.ip());
        
        res.status(400);
        res.type("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            "Bad Request",
            ex.getMessage(),
            req.pathInfo(),
            LocalDateTime.now().format(formatter)
        );
        
        if (ex.getField() != null) {
            errorResponse.setField(ex.getField());
        }
        
        return gson.toJson(errorResponse);
    }

    /**
     * Handles ServerException (500)
     * Returns a standardized error response for server errors
     */
    public static String handleServerException(ServerException ex, Request req, Response res) {
        logger.error("Server error: {} - Operation: {} - Path: {}", 
            ex.getMessage(), ex.getOperation(), req.pathInfo(), ex);
        
        res.status(500);
        res.type("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            req.pathInfo(),
            LocalDateTime.now().format(formatter)
        );
        
        return gson.toJson(errorResponse);
    }

    /**
     * Handles generic exceptions (500)
     * Catches any unexpected errors and returns a safe response
     */
    public static String handleGenericException(Exception ex, Request req, Response res) {
        logger.error("Unexpected error: {} - Path: {}", ex.getMessage(), req.pathInfo(), ex);
        
        res.status(500);
        res.type("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            req.pathInfo(),
            LocalDateTime.now().format(formatter)
        );
        
        return gson.toJson(errorResponse);
    }

    /**
     * Standard Error Response Format
     * Provides consistent error information to API clients
     */
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private final String timestamp;
        private String field; // Optional: for validation errors

        public ErrorResponse(int status, String error, String message, String path, String timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }

        // Getters
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public String getTimestamp() { return timestamp; }
        public String getField() { return field; }

        // Setter
        public void setField(String field) { this.field = field; }
    }
}
