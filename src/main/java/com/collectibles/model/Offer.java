package com.collectibles.model;

/**
 * Represents an offer made by a potential buyer for a collectible item.
 * Contains buyer information (name, email) and offer details (item reference, amount).
 */
public class Offer {
    private String id;
    private String name;
    private String email;
    private String itemId;  // Reference to the item being offered on
    private double amount;

    /**
     * Default constructor for JSON deserialization
     */
    public Offer() {
    }

    /**
     * Constructor with all fields
     * @param id Unique identifier for the offer
     * @param name Name of the person making the offer
     * @param email Email contact of the person making the offer
     * @param itemId ID of the item this offer is for
     * @param amount Proposed amount for the item
     */
    public Offer(String id, String name, String email, String itemId, double amount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.itemId = itemId;
        this.amount = amount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns a formatted string representation of the offer amount
     * @return Formatted amount with dollar sign (e.g., "$850.00")
     */
    public String getFormattedAmount() {
        return String.format("$%.2f", amount);
    }

    @Override
    public String toString() {
        return "Offer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", itemId='" + itemId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
