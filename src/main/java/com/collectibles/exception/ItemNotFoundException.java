package com.collectibles.exception;

/**
 * Exception thrown when a requested item is not found in the database
 * HTTP Status: 404 Not Found
 */
public class ItemNotFoundException extends RuntimeException {
    private final String itemId;

    public ItemNotFoundException(String itemId) {
        super(String.format("Item not found with ID: %s", itemId));
        this.itemId = itemId;
    }

    public ItemNotFoundException(String itemId, String message) {
        super(message);
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}
