package com.collectibles.controller;

import com.collectibles.model.Item;
import com.collectibles.exception.ItemNotFoundException;
import com.collectibles.exception.ServerException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.Route;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.collectibles.database.DatabaseConfig;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller responsible for item-related operations.
 * Exposes concise REST endpoints for retrieving item summaries and
 * detailed item records for UI templates and API consumers.
 *
 * <p>Primary operations:
 * <ul>
 *   <li>Retrieve all items with basic information (ID, name, price)</li>
 *   <li>Retrieve detailed item information by ID including description</li>
 *   <li>Retrieve only the description of a specific item</li>
 * </ul>
 * 
 * <p><b>Sprint 2 Update:</b> Enhanced with exception handling for 404 and 500 errors</p>
 *
 * Authors: Ricardo Ruiz and Melany Rivera
 * @since 27/10/2025
 * @version 2.0
 */
public class ItemController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final Gson gson = new Gson();
    private final Map<String, Item> itemDatabase = new HashMap<>();

    /**
     * Constructs a new ItemController and loads items from the JSON file.
     * Initializes the item database by reading from items.json resource.
     * 
     * @throws ServerException if items.json cannot be loaded
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
     * @throws ServerException if there's an error loading the JSON file
     * @since 27/10/2025
     */
    private void loadItemsFromFile() {
        // Load items from the database instead of the static JSON file
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT id, name, price, description FROM items";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Item item = new Item();
                    String id = rs.getString("id");
                    item.setId(id);
                    item.setName(rs.getString("name"));
                    // price is stored as DECIMAL - read as string and format if necessary
                    double priceVal = rs.getDouble("price");
                    item.setPrice(String.format("$%.2f USD", priceVal));
                    item.setDescription(rs.getString("description"));
                    itemDatabase.put(item.getId(), item);
                    count++;
                }
                if (count == 0) {
                    logger.warn("No items found in database 'items' table");
                    throw new ServerException("loadItems", "No items found in database");
                }
                logger.info("Items loaded from database: {} items", itemDatabase.size());
            }
        } catch (SQLException e) {
            logger.error("Error loading items from database: {}", e.getMessage(), e);
            throw new ServerException("loadItems", "Failed to load items from database", e);
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
     * @throws ServerException if there's an error processing the request
     * @since 27/10/2025
     */
    public Route getAllItems() {
        return (req, res) -> {
            try {
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
                
            } catch (Exception e) {
                logger.error("Error retrieving items list", e);
                throw new ServerException("getAllItems", "Failed to retrieve items", e);
            }
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
     * <p><b>Error Response (404 Not Found):</b></p>
     * <pre>
     * {
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Item not found with ID: item999",
     *   "path": "/items/item999",
     *   "timestamp": "2025-10-27 14:30:00"
     * }
     * </pre>
     * 
     * @return Route handler for retrieving an item by ID
     * @throws ItemNotFoundException if item with given ID doesn't exist (404)
     * @throws ServerException if there's an error processing the request (500)
     * @since 27/10/2025
     */
    public Route getItemById() {
        return (req, res) -> {
            try {
                String id = req.params(":id");
                logger.info("GET /items/{} - Retrieving item description", id);

                Item item = itemDatabase.get(id);
                
                if (item == null) {
                    // Return 404 JSON directly (unit tests call routes directly, bypassing global exception handler)
                    res.type("application/json");
                    res.status(404);
                    ApiResponse notFound = new ApiResponse(false, "Item not found with ID: " + id, null);
                    return gson.toJson(notFound);
                }

                ApiResponse response = new ApiResponse(
                    true,
                    "Item found",
                    item
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
                
            } catch (ItemNotFoundException e) {
                throw e; // Re-throw to be handled by exception handler
            } catch (Exception e) {
                logger.error("Error retrieving item by ID", e);
                throw new ServerException("getItemById", "Failed to retrieve item", e);
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
     * @throws ItemNotFoundException if item with given ID doesn't exist (404)
     * @throws ServerException if there's an error processing the request (500)
     * @since 27/10/2025
     */
    public Route getItemDescription() {
        return (req, res) -> {
            try {
                String id = req.params(":id");
                logger.info("GET /items/{}/description - Retrieving description only", id);

                Item item = itemDatabase.get(id);
                
                if (item == null) {
                    // Return 404 JSON directly for tests that call the route directly
                    res.type("application/json");
                    res.status(404);
                    ApiResponse notFound = new ApiResponse(false, "Item not found with ID: " + id, null);
                    return gson.toJson(notFound);
                }

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
                
            } catch (ItemNotFoundException e) {
                throw e; // Re-throw to be handled by exception handler
            } catch (Exception e) {
                logger.error("Error retrieving item description", e);
                throw new ServerException("getItemDescription", "Failed to retrieve description", e);
            }
        };
    }

    /**
    * Internal value object for standardized API responses.
    * This simple DTO centralizes the response contract used across
    * controller endpoints (success, message, data).
    *
    * Authors: Ricardo Ruiz and Melany Rivera
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

    // ==================== TEMPLATE RENDERING ROUTES (Sprint 2) ====================

    /**
     * Route handler that renders the items list page using Mustache template.
     * Displays all items with their names and prices in an HTML table.
     * 
     * <p><b>Sprint 2 Feature:</b> Template rendering for web UI
     * 
     * @return ModelAndView containing items data and template name
     * @since 27/10/2025
     */
    public ModelAndView renderItemsListPage() {
        logger.info("GET /items - Rendering items list page");
        
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> itemsList = new ArrayList<>();
        
        // Convert items to a list format suitable for the template
        for (Item item : itemDatabase.values()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            // Derive image filename from id (e.g., item1 -> item 1.jpg)
            String imageName = deriveImageName(item.getId());
            itemMap.put("image", imageName);
            // add artist label for UI (optional display)
            String artist = deriveArtistName(item.getId());
            itemMap.put("artist", artist);
            itemsList.add(itemMap);
        }
        
        model.put("items", itemsList);
        
        return new ModelAndView(model, "items.mustache");
    }

    /**
     * Return a list of item maps suitable for templates (id, name, price, image, artist).
     * Used by the home page and other templates.
     */
    public List<Map<String, Object>> getItemsForTemplate() {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (Item item : itemDatabase.values()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("description", item.getDescription());
            itemMap.put("image", deriveImageName(item.getId()));
            itemMap.put("artist", deriveArtistName(item.getId()));
            itemsList.add(itemMap);
        }
        return itemsList;
    }

    /**
     * Route handler that renders the item detail page using Mustache template.
     * Displays complete item information including description.
     * 
     * <p><b>Sprint 2 Feature:</b> Template rendering for web UI
     * 
     * @param id The unique identifier of the item
     * @return ModelAndView containing item data and template name
     * @throws ItemNotFoundException if item with given ID doesn't exist (404)
     * @since 27/10/2025
     */
    public ModelAndView renderItemDetailPage(String id) {
        logger.info("GET /items/{} - Rendering item detail page", id);
        
        Item item = itemDatabase.get(id);
        if (item == null) {
            logger.warn("Item not found for detail page: {}", id);
            throw new ItemNotFoundException(id);
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("id", item.getId());
        model.put("name", item.getName());
        model.put("price", item.getPrice());
        model.put("description", item.getDescription());
        model.put("image", deriveImageName(item.getId()));
        model.put("artist", deriveArtistName(item.getId()));
        // include offers for this item (if any)
        try {
            java.util.List<java.util.Map<String, Object>> itemOffers = OfferController.getOffersForItemModel(item.getId());
            model.put("offers", itemOffers);
            model.put("hasOffers", !itemOffers.isEmpty());
        } catch (Exception e) {
            // Failure to load offers should not prevent rendering the item page
            logger.warn("Could not load offers for item {}: {}", item.getId(), e.getMessage());
            model.put("offers", new java.util.ArrayList<>());
            model.put("hasOffers", false);
        }
        
        return new ModelAndView(model, "item-detail.mustache");
    }

    /**
     * Derive a friendly image filename for an item id.
     * Example: item1 -> item 1.jpg
     */
    private String deriveImageName(String id) {
        if (id == null) return "placeholder.jpg";
        // Map specific items to artist image names (user-requested order)
        switch (id) {
            case "item1": return "rosalia.jpg";      // item 1 -> rosalia
            case "item2": return "peso pluma.jpg"; // item 2 -> peso pluma
            case "item3": return "coldplay.jpg";   // item 3 -> coldplay
            case "item4": return "guitarra.jpg";   // item 4 -> guitarra
            case "item5": return "bad bunny.jpg";  // item 5 -> bad bunny
            case "item6": return "cardi b.jpg";    // item 6 -> cardi b
            case "item7": return "snoop dogg.jpg"; // item 7 -> snoop dogg
            default:
                // if id matches itemN (where N is digits), default to existing files
                if (id.matches("item\\d+")) {
                    String num = id.replaceAll("[^0-9]", "");
                    return "item " + num + ".jpg";
                }
                // fallback: use id with .jpg
                return id + ".jpg";
        }
    }

    /**
     * Human-friendly artist name mapping for each item id.
     */
    private String deriveArtistName(String id) {
        if (id == null) return "";
        switch (id) {
            case "item1": return "Rosalía";
            case "item2": return "Peso Pluma";
            case "item3": return "Coldplay";
            case "item4": return "Guitarra";
            case "item5": return "Bad Bunny";
            case "item6": return "Cardi B";
            case "item7": return "Snoop Dogg";
            default: return "";
        }
    }
}
