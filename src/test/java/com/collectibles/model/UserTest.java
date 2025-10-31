package com.collectibles.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Executive unit tests for the User model.
 * These tests validate user data handling, role behaviors, and
 * automatic timestamp generation to ensure consistent user records.
 *
 * Authors: Ricardo Ruiz and Melany Rivera
 * @since 27/10/2025
 * @version 1.0
 */
@DisplayName("User Model Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create User with default constructor")
    void testDefaultConstructor() {
        assertNotNull(user, "User should not be null");
        assertNull(user.getId(), "ID should be null");
        assertNull(user.getName(), "Name should be null");
        assertNull(user.getEmail(), "Email should be null");
        assertNull(user.getRole(), "Role should be null");
        assertNull(user.getCreatedAt(), "CreatedAt should be null");
    }

    @Test
    @DisplayName("Should create User with parameterized constructor")
    void testParameterizedConstructor() {
        String id = "1";
        String name = "John Doe";
        String email = "john@example.com";
        String role = "admin";
        
        User user = new User(id, name, email, role);
        
        assertNotNull(user, "User should not be null");
        assertEquals(id, user.getId(), "ID should match");
        assertEquals(name, user.getName(), "Name should match");
        assertEquals(email, user.getEmail(), "Email should match");
        assertEquals(role, user.getRole(), "Role should match");
        assertNotNull(user.getCreatedAt(), "CreatedAt should be set automatically");
    }

    @Test
    @DisplayName("Should set and get ID correctly")
    void testSetAndGetId() {
        String id = "123";
        user.setId(id);
        
        assertEquals(id, user.getId(), "ID should be set and retrieved correctly");
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        String name = "Jane Smith";
        user.setName(name);
        
        assertEquals(name, user.getName(), "Name should be set and retrieved correctly");
    }

    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        String email = "jane@example.com";
        user.setEmail(email);
        
        assertEquals(email, user.getEmail(), "Email should be set and retrieved correctly");
    }

    @Test
    @DisplayName("Should set and get role correctly")
    void testSetAndGetRole() {
        String role = "buyer";
        user.setRole(role);
        
        assertEquals(role, user.getRole(), "Role should be set and retrieved correctly");
    }

    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testSetAndGetCreatedAt() {
        String timestamp = "2025-10-27T10:00:00";
        user.setCreatedAt(timestamp);
        
        assertEquals(timestamp, user.getCreatedAt(), "CreatedAt should be set and retrieved correctly");
    }

    @Test
    @DisplayName("Should return correct toString representation")
    void testToString() {
        String id = "1";
        String name = "John Doe";
        String email = "john@example.com";
        String role = "admin";
        
        User user = new User(id, name, email, role);
        String toString = user.toString();
        
        assertNotNull(toString, "ToString should not be null");
        assertTrue(toString.contains(id), "ToString should contain ID");
        assertTrue(toString.contains(name), "ToString should contain name");
        assertTrue(toString.contains(email), "ToString should contain email");
        assertTrue(toString.contains(role), "ToString should contain role");
        assertTrue(toString.contains("createdAt"), "ToString should contain createdAt field");
    }

    @Test
    @DisplayName("Should automatically set creation timestamp")
    void testCreationTimestamp() {
        User newUser = new User("1", "Test User", "test@example.com", "admin");
        
        assertNotNull(newUser.getCreatedAt(), "Creation timestamp should be set");
        assertFalse(newUser.getCreatedAt().isEmpty(), "Creation timestamp should not be empty");
    }

    @Test
    @DisplayName("Should handle different user roles")
    void testDifferentRoles() {
        user.setRole("admin");
        assertEquals("admin", user.getRole());
        
        user.setRole("seller");
        assertEquals("seller", user.getRole());
        
        user.setRole("buyer");
        assertEquals("buyer", user.getRole());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        User nullUser = new User(null, null, null, null);
        
        assertNull(nullUser.getId(), "ID should be null");
        assertNull(nullUser.getName(), "Name should be null");
        assertNull(nullUser.getEmail(), "Email should be null");
        assertNull(nullUser.getRole(), "Role should be null");
        assertNotNull(nullUser.getCreatedAt(), "CreatedAt should still be set");
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        User emptyUser = new User("", "", "", "");
        
        assertEquals("", emptyUser.getId(), "ID should be empty string");
        assertEquals("", emptyUser.getName(), "Name should be empty string");
        assertEquals("", emptyUser.getEmail(), "Email should be empty string");
        assertEquals("", emptyUser.getRole(), "Role should be empty string");
    }
}
