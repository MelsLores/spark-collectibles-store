package com.collectibles.controller;

import com.collectibles.model.Item;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import spark.Request;
import spark.Response;
import spark.Route;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ItemController class.
 * Tests all route handlers and business logic methods.
 * 
 * @author Melany Rivera
 * @since 27/10/2025
 * @version 1.0
 */
@DisplayName("ItemController Tests")
class ItemControllerTest {

    private ItemController controller;
    private Request mockRequest;
    private Response mockResponse;
    private Gson gson;

    @BeforeEach
    void setUp() {
        controller = new ItemController();
        mockRequest = mock(Request.class);
        mockResponse = mock(Response.class);
        gson = new Gson();
    }

    @Test
    @DisplayName("Should initialize controller successfully")
    void testControllerInitialization() {
        assertNotNull(controller, "Controller should be initialized");
    }

    @Test
    @DisplayName("Should get all items successfully")
    void testGetAllItems() throws Exception {
        Route route = controller.getAllItems();
        Object result = route.handle(mockRequest, mockResponse);
        
        assertNotNull(result, "Result should not be null");
        verify(mockResponse).type("application/json");
        
        String jsonResult = (String) result;
        assertFalse(jsonResult.isEmpty(), "JSON result should not be empty");
        assertTrue(jsonResult.contains("success"), "Result should contain success field");
        assertTrue(jsonResult.contains("data"), "Result should contain data array");
    }

    @Test
    @DisplayName("Should get item by valid ID")
    void testGetItemById_ValidId() throws Exception {
        when(mockRequest.params(":id")).thenReturn("item1");
        
        Route route = controller.getItemById();
        Object result = route.handle(mockRequest, mockResponse);
        
        verify(mockResponse).type("application/json");
        String jsonResult = (String) result;
        
        assertTrue(jsonResult.contains("success"), "Result should contain success field");
        assertTrue(jsonResult.contains("item1"), "Result should contain the requested item ID");
        assertTrue(jsonResult.contains("name"), "Result should contain item name");
    }

    @Test
    @DisplayName("Should return error for invalid item ID")
    void testGetItemById_InvalidId() throws Exception {
        when(mockRequest.params(":id")).thenReturn("invalidItemId");
        
        Route route = controller.getItemById();
        Object result = route.handle(mockRequest, mockResponse);
        
        verify(mockResponse).status(404);
        verify(mockResponse).type("application/json");
        
        String jsonResult = (String) result;
        assertTrue(jsonResult.contains("success"), "Result should contain success field");
        JsonObject json = JsonParser.parseString(jsonResult).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "Success should be false for invalid ID");
    }

    @Test
    @DisplayName("Should get item description by valid ID")
    void testGetItemDescription_ValidId() throws Exception {
        when(mockRequest.params(":id")).thenReturn("item1");
        
        Route route = controller.getItemDescription();
        Object result = route.handle(mockRequest, mockResponse);
        
        verify(mockResponse).type("application/json");
        String jsonResult = (String) result;
        
        assertTrue(jsonResult.contains("success"), "Result should contain success field");
        assertTrue(jsonResult.contains("id"), "Result should contain item ID");
        assertTrue(jsonResult.contains("description"), "Result should contain description");
    }

    @Test
    @DisplayName("Should return error for invalid ID when getting description")
    void testGetItemDescription_InvalidId() throws Exception {
        when(mockRequest.params(":id")).thenReturn("nonExistentItem");
        
        Route route = controller.getItemDescription();
        Object result = route.handle(mockRequest, mockResponse);
        
        verify(mockResponse).status(404);
        verify(mockResponse).type("application/json");
        
        String jsonResult = (String) result;
        JsonObject json = JsonParser.parseString(jsonResult).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "Success should be false for invalid ID");
    }

    @Test
    @DisplayName("Should handle null request parameter")
    void testGetItemById_NullParameter() throws Exception {
        when(mockRequest.params(":id")).thenReturn(null);
        
        Route route = controller.getItemById();
        Object result = route.handle(mockRequest, mockResponse);
        
        verify(mockResponse).status(404);
        verify(mockResponse).type("application/json");
    }

    @Test
    @DisplayName("Should return JSON with correct content type")
    void testResponseContentType() throws Exception {
        Route getAllRoute = controller.getAllItems();
        getAllRoute.handle(mockRequest, mockResponse);
        verify(mockResponse).type("application/json");
        
        when(mockRequest.params(":id")).thenReturn("item1");
        Route getByIdRoute = controller.getItemById();
        getByIdRoute.handle(mockRequest, mockResponse);
        verify(mockResponse, times(2)).type("application/json");
        
        Route getDescRoute = controller.getItemDescription();
        getDescRoute.handle(mockRequest, mockResponse);
        verify(mockResponse, times(3)).type("application/json");
    }

    @Test
    @DisplayName("Should validate response structure for getAllItems")
    void testGetAllItemsResponseStructure() throws Exception {
        Route route = controller.getAllItems();
        Object result = route.handle(mockRequest, mockResponse);
        
        String jsonResult = (String) result;
        JsonObject json = JsonParser.parseString(jsonResult).getAsJsonObject();
        
        assertTrue(json.has("success"), "Response should have success field");
        assertTrue(json.has("message"), "Response should have message field");
        assertTrue(json.has("data"), "Response should have data field");
        assertTrue(json.get("data").isJsonArray(), "Data field should be an array");
    }

    @Test
    @DisplayName("Should validate response structure for getItemById")
    void testGetItemByIdResponseStructure() throws Exception {
        when(mockRequest.params(":id")).thenReturn("item1");
        
        Route route = controller.getItemById();
        Object result = route.handle(mockRequest, mockResponse);
        
        String jsonResult = (String) result;
        JsonObject json = JsonParser.parseString(jsonResult).getAsJsonObject();
        
        assertTrue(json.has("success"), "Response should have success field");
        assertTrue(json.has("message"), "Response should have message field");
        assertTrue(json.has("data"), "Response should have data field");
        assertTrue(json.get("data").isJsonObject(), "Data field should be an object");
    }
}
