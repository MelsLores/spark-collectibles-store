package com.collectibles.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Item model class.
 * Tests all constructors, getters, setters, and utility methods.
 * 
 * <p><b>Test Coverage:</b></p>
 * <ul>
 * <li>Default constructor</li>
 * <li>Parameterized constructor</li>
 * <li>Getter and setter methods</li>
 * <li>toString() method</li>
 * <li>Edge cases (null values, empty strings)</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
@DisplayName("Item Model Tests")
class ItemTest {

    /**
     * Tests the default constructor.
     * Verifies all fields are initialized to null.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should create Item with default constructor")
    void testDefaultConstructor() {
        Item item = new Item();
        
        assertNotNull(item, "Item should not be null");
        assertNull(item.getId(), "ID should be null");
        assertNull(item.getName(), "Name should be null");
        assertNull(item.getDescription(), "Description should be null");
        assertNull(item.getPrice(), "Price should be null");
    }

    /**
     * Tests the parameterized constructor.
     * Verifies all fields are correctly initialized with provided values.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should create Item with parameterized constructor")
    void testParameterizedConstructor() {
        String id = "item1";
        String name = "Test Item";
        String description = "Test Description";
        String price = "$100.00 USD";
        
        Item item = new Item(id, name, description, price);
        
        assertNotNull(item, "Item should not be null");
        assertEquals(id, item.getId(), "ID should match");
        assertEquals(name, item.getName(), "Name should match");
        assertEquals(description, item.getDescription(), "Description should match");
        assertEquals(price, item.getPrice(), "Price should match");
    }

    /**
     * Tests the ID getter and setter methods.
     * Verifies proper assignment and retrieval.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get ID correctly")
    void testSetAndGetId() {
        Item item = new Item();
        String id = "item123";
        
        item.setId(id);
        
        assertEquals(id, item.getId(), "ID should be set and retrieved correctly");
    }

    /**
     * Tests the name getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        Item item = new Item();
        String name = "Collectible Item";
        
        item.setName(name);
        
        assertEquals(name, item.getName(), "Name should be set and retrieved correctly");
    }

    /**
     * Tests the description getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get description correctly")
    void testSetAndGetDescription() {
        Item item = new Item();
        String description = "A rare collectible item";
        
        item.setDescription(description);
        
        assertEquals(description, item.getDescription(), "Description should be set and retrieved correctly");
    }

    /**
     * Tests the price getter and setter methods.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should set and get price correctly")
    void testSetAndGetPrice() {
        Item item = new Item();
        String price = "$999.99 USD";
        
        item.setPrice(price);
        
        assertEquals(price, item.getPrice(), "Price should be set and retrieved correctly");
    }

    /**
     * Tests the toString() method.
     * Verifies that all fields are included in string representation.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should return correct toString representation")
    void testToString() {
        String id = "item1";
        String name = "Test Item";
        String description = "Test Description";
        String price = "$100.00 USD";
        
        Item item = new Item(id, name, description, price);
        String toString = item.toString();
        
        assertNotNull(toString, "ToString should not be null");
        assertTrue(toString.contains(id), "ToString should contain ID");
        assertTrue(toString.contains(name), "ToString should contain name");
        assertTrue(toString.contains(description), "ToString should contain description");
        assertTrue(toString.contains(price), "ToString should contain price");
    }

    /**
     * Tests handling of null values in constructor.
     * Verifies that null values are properly stored.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        Item item = new Item(null, null, null, null);
        
        assertNull(item.getId(), "ID should be null");
        assertNull(item.getName(), "Name should be null");
        assertNull(item.getDescription(), "Description should be null");
        assertNull(item.getPrice(), "Price should be null");
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
        Item item = new Item("", "", "", "");
        
        assertEquals("", item.getId(), "ID should be empty string");
        assertEquals("", item.getName(), "Name should be empty string");
        assertEquals("", item.getDescription(), "Description should be empty string");
        assertEquals("", item.getPrice(), "Price should be empty string");
    }
}
