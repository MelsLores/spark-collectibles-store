package com.collectibles.model;

/**
 * Represents a user in the collectibles store system.
 * This model encapsulates user information including authentication
 * and authorization details for the online store platform.
 * 
 * @author Melany Rivera
 * @since 27/10/2025
 * @version 1.0
 */
public class User {
    private String id;
    private String name;
    private String email;
    private String role;
    private String createdAt;

    /**
     * Default constructor for User.
     * Creates an empty User instance.
     * 
     * @since 27/10/2025
     */
    public User() {
    }

    /**
     * Parameterized constructor for User.
     * Creates a new User with specified properties and sets creation timestamp.
     * 
     * @param id The unique identifier for the user
     * @param name The full name of the user
     * @param email The email address of the user
     * @param role The role of the user (admin, seller, buyer)
     * @since 27/10/2025
     */
    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    /**
     * Gets the unique identifier of the user.
     * 
     * @return The user's unique ID
     * @since 27/10/2025
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     * 
     * @param id The unique ID to set
     * @since 27/10/2025
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the user.
     * 
     * @return The user's full name
     * @since 27/10/2025
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     * 
     * @param name The name to set
     * @since 27/10/2025
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the user.
     * 
     * @return The user's email address
     * @since 27/10/2025
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     * 
     * @param email The email address to set
     * @since 27/10/2025
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the role of the user.
     * 
     * @return The user's role (admin, seller, buyer)
     * @since 27/10/2025
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     * 
     * @param role The role to set
     * @since 27/10/2025
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the creation timestamp of the user.
     * 
     * @return The timestamp when the user was created
     * @since 27/10/2025
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the user.
     * 
     * @param createdAt The creation timestamp to set
     * @since 27/10/2025
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the User.
     * 
     * @return A formatted string containing all user properties
     * @since 27/10/2025
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
