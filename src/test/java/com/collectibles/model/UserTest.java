package com.collectibles.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User model class.
 * Tests all constructors, getters, setters, and utility methods.
 * 
 * <p><b>Test Coverage:</b></p>
 * <ul>
 * <li>Default constructor</li>
 * <li>Parameterized constructor with automatic timestamp</li>
 * <li>Getter and setter methods for all fields</li>
 * <li>toString() method representation</li>
 * <li>Automatic creation timestamp generation</li>
 * <li>Different user roles (admin, seller, buyer)</li>
 * <li>Edge cases (null values, empty strings)</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
@DisplayName("User Model Tests")
class UserTest {

    private User user;

    /**
     * Sets up test fixtures before each test execution.
     * Initializes a new User instance with default constructor.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @BeforeEach
    void setUp() {
        user = new User();
    }

    /**
     * Tests the default constructor.
     * Verifies all fields are initialized to null.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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

    /**
     * Tests the parameterized constructor.
     * Verifies all fields are correctly initialized and timestamp is auto-generated.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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

    /**
     * Tests ID getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get ID correctly")
    void testSetAndGetId() {
        String id = "123";
        user.setId(id);
        
        assertEquals(id, user.getId(), "ID should be set and retrieved correctly");
    }

    /**
     * Tests name getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        String name = "Jane Smith";
        user.setName(name);
        
        assertEquals(name, user.getName(), "Name should be set and retrieved correctly");
    }

    /**
     * Tests email getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        String email = "jane@example.com";
        user.setEmail(email);
        
        assertEquals(email, user.getEmail(), "Email should be set and retrieved correctly");
    }

    /**
     * Tests role getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get role correctly")
    void testSetAndGetRole() {
        String role = "buyer";
        user.setRole(role);
        
        assertEquals(role, user.getRole(), "Role should be set and retrieved correctly");
    }

    /**
     * Tests createdAt getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testSetAndGetCreatedAt() {
        String timestamp = "2025-10-27T10:00:00";
        user.setCreatedAt(timestamp);
        
        assertEquals(timestamp, user.getCreatedAt(), "CreatedAt should be set and retrieved correctly");
    }

    /**
     * Tests the toString() method.
     * Verifies all fields are included in string representation.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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

    /**
     * Tests automatic creation timestamp generation.
     * Verifies that createdAt is automatically set when user is created.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should automatically set creation timestamp")
    void testCreationTimestamp() {
        User newUser = new User("1", "Test User", "test@example.com", "admin");
        
        assertNotNull(newUser.getCreatedAt(), "Creation timestamp should be set");
        assertFalse(newUser.getCreatedAt().isEmpty(), "Creation timestamp should not be empty");
    }

    /**
     * Tests handling of different user roles.
     * Verifies that role field accepts various role types.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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

    /**
     * Tests handling of null values in constructor.
     * Verifies that null values are properly stored except for auto-generated createdAt.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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

    /**
     * Tests handling of empty strings in constructor.
     * Verifies that empty strings are properly stored.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
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
