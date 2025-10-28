package com.collectibles.model;

/**
 * Represents a collectible item in the online store.
 * This model encapsulates all the information about a collectible product
 * including its unique identifier, name, detailed description, and price.
 * 
 * @author Melany Rivera
 * @since 27/10/2025
 * @version 1.0
 */
public class Item {
    private String id;
    private String name;
    private String description;
    private String price;

    /**
     * Default constructor for Item.
     * Creates an empty Item instance.
     * 
     * @since 27/10/2025
     */
    public Item() {
    }

    /**
     * Parameterized constructor for Item.
     * Creates a new Item with all properties initialized.
     * 
     * @param id The unique identifier for the item
     * @param name The display name of the collectible item
     * @param description A detailed description of the item
     * @param price The price of the item in USD format
     * @since 27/10/2025
     */
    public Item(String id, String name, String description, String price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    /**
     * Gets the unique identifier of the item.
     * 
     * @return The item's unique ID
     * @since 27/10/2025
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the item.
     * 
     * @param id The unique ID to set
     * @since 27/10/2025
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the item.
     * 
     * @return The item's name
     * @since 27/10/2025
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     * 
     * @param name The name to set
     * @since 27/10/2025
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the detailed description of the item.
     * 
     * @return The item's description
     * @since 27/10/2025
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the detailed description of the item.
     * 
     * @param description The description to set
     * @since 27/10/2025
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the price of the item.
     * 
     * @return The item's price in USD format
     * @since 27/10/2025
     */
    public String getPrice() {
        return price;
    }

    /**
     * Sets the price of the item.
     * 
     * @param price The price to set in USD format
     * @since 27/10/2025
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * Returns a string representation of the Item.
     * 
     * @return A formatted string containing all item properties
     * @since 27/10/2025
     */
    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
