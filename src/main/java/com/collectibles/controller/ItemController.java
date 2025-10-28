package com.collectibles.controller;

import com.collectibles.model.Item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing collectible items in the online store.
 * Provides REST API endpoints for retrieving and querying collectible items
 * loaded from a JSON data source.
 * 
 * <p>This controller implements the following operations:
 * <ul>
 *   <li>Retrieve all items with basic information (ID, name, price)</li>
 *   <li>Retrieve detailed item information by ID including description</li>
 *   <li>Retrieve only the description of a specific item</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @since 27/10/2025
 * @version 1.0
 */
public class ItemController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final Gson gson = new Gson();
    private final Map<String, Item> itemDatabase = new HashMap<>();

    /**
     * Constructs a new ItemController and loads items from the JSON file.
     * Initializes the item database by reading from items.json resource.
     * 
     * @since 27/10/2025
     */
    public ItemController() {
        loadItemsFromFile();
    }

    /**
     * Loads collectible items from the items.json resource file.
     * Parses the JSON content and populates the internal item database
     * using item IDs as keys for quick retrieval.
     * 
     * @since 27/10/2025
     */
    private void loadItemsFromFile() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("items.json");
            
            if (inputStream == null) {
                logger.error("Could not find items.json file");
                return;
            }

            Type itemListType = new TypeToken<ArrayList<Item>>(){}.getType();
            List<Item> items = gson.fromJson(new InputStreamReader(inputStream), itemListType);

            for (Item item : items) {
                itemDatabase.put(item.getId(), item);
            }

            logger.info("Items loaded from items.json: {} items", itemDatabase.size());

        } catch (Exception e) {
            logger.error("Error loading items from JSON file: ", e);
        }
    }

    /**
     * Returns a Spark Route that handles GET requests to retrieve all items.
     * Returns a simplified list containing only ID, name, and price for each item.
     * 
     * <p><b>Endpoint:</b> GET /items</p>
     * <p><b>Response:</b> JSON with list of items containing {id, name, price}</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Items retrieved successfully",
     *   "data": [
     *     {
     *       "id": "item1",
     *       "name": "Peso Pluma Signed Cap",
     *       "price": "$621.34 USD"
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @return Route handler for retrieving all items
     * @since 27/10/2025
     */
    public Route getAllItems() {
        return (req, res) -> {
            logger.info("GET /items - Retrieving items list");
            
            List<Map<String, String>> simplifiedItems = itemDatabase.values().stream()
                .map(item -> {
                    Map<String, String> itemSummary = new HashMap<>();
                    itemSummary.put("id", item.getId());
                    itemSummary.put("name", item.getName());
                    itemSummary.put("price", item.getPrice());
                    return itemSummary;
                })
                .collect(Collectors.toList());

            ApiResponse response = new ApiResponse(
                true,
                "Items retrieved successfully",
                simplifiedItems
            );
            
            res.type("application/json");
            res.status(200);
            return gson.toJson(response);
        };
    }

    /**
     * Returns a Spark Route that handles GET requests to retrieve an item by ID.
     * Returns complete item information including the full description.
     * 
     * <p><b>Endpoint:</b> GET /items/:id</p>
     * <p><b>Parameter:</b> id - The unique identifier of the item (e.g., "item1", "item2")</p>
     * <p><b>Response:</b> JSON with complete item object {id, name, description, price}</p>
     * 
     * <p><b>Example Response (Success):</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Item found",
     *   "data": {
     *     "id": "item3",
     *     "name": "Bad Bunny Jacket",
     *     "description": "A jacket from Bad Bunny's favorite brand...",
     *     "price": "$521.89 USD"
     *   }
     * }
     * </pre>
     * 
     * <p><b>Example Response (Not Found):</b></p>
     * <pre>
     * {
     *   "success": false,
     *   "message": "Item not found with ID: item999",
     *   "data": null
     * }
     * </pre>
     * 
     * @return Route handler for retrieving an item by ID
     * @since 27/10/2025
     */
    public Route getItemById() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("GET /items/{} - Retrieving item description", id);

            Item item = itemDatabase.get(id);
            
            if (item != null) {
                ApiResponse response = new ApiResponse(
                    true,
                    "Item found",
                    item
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
            } else {
                ApiResponse response = new ApiResponse(
                    false,
                    "Item not found with ID: " + id,
                    null
                );
                res.type("application/json");
                res.status(404);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Returns a Spark Route that handles GET requests to retrieve only an item's description.
     * This is an alternative endpoint that returns specifically the description field
     * along with ID and name.
     * 
     * <p><b>Endpoint:</b> GET /items/:id/description</p>
     * <p><b>Parameter:</b> id - The unique identifier of the item</p>
     * <p><b>Response:</b> JSON with {id, name, description}</p>
     * 
     * <p><b>Example Response:</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Item description retrieved",
     *   "data": {
     *     "id": "item5",
     *     "name": "Snoop Dogg Signed Jersey",
     *     "description": "A jersey signed by legendary rapper Snoop Dogg."
     *   }
     * }
     * </pre>
     * 
     * @return Route handler for retrieving an item's description
     * @since 27/10/2025
     */
    public Route getItemDescription() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("GET /items/{}/description - Retrieving description only", id);

            Item item = itemDatabase.get(id);
            
            if (item != null) {
                Map<String, String> descriptionData = new HashMap<>();
                descriptionData.put("id", item.getId());
                descriptionData.put("name", item.getName());
                descriptionData.put("description", item.getDescription());
                
                ApiResponse response = new ApiResponse(
                    true,
                    "Item description retrieved",
                    descriptionData
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
            } else {
                ApiResponse response = new ApiResponse(
                    false,
                    "Item not found with ID: " + id,
                    null
                );
                res.type("application/json");
                res.status(404);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Internal class for standardized API responses.
     * Provides a consistent response structure across all endpoints.
     * 
     * @author Melany Rivera
     * @since 27/10/2025
     */
    private static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        /**
         * Constructs a new API response.
         * 
         * @param success Whether the operation was successful
         * @param message A descriptive message about the operation result
         * @param data The data payload (can be null for errors)
         * @since 27/10/2025
         */
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        /**
         * Gets the success status of the operation.
         * 
         * @return true if successful, false otherwise
         * @since 27/10/2025
         */
        public boolean isSuccess() { return success; }
        
        /**
         * Gets the message describing the operation result.
         * 
         * @return The result message
         * @since 27/10/2025
         */
        public String getMessage() { return message; }
        
        /**
         * Gets the data payload of the response.
         * 
         * @return The response data
         * @since 27/10/2025
         */
        public Object getData() { return data; }
    }
}
