package com.collectibles.controller;

import com.collectibles.exception.InvalidRequestException;
import com.collectibles.exception.ItemNotFoundException;
import com.collectibles.model.Offer;
import com.collectibles.model.Item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for handling offer-related operations.
 * Manages offer submissions, listing, form display, and persistence.
 * 
 * <p><b>Features:</b></p>
 * <ul>
 * <li>Create and submit offers for items</li>
 * <li>List all offers with filtering by item</li>
 * <li>Render offer forms and lists using Mustache templates</li>
 * <li>Persist offers to JSON file for data durability</li>
 * </ul>
 * 
 * <p><b>Endpoints:</b></p>
 * <ul>
 * <li>GET /offers/new - Display offer submission form</li>
 * <li>POST /offers - Submit new offer (API)</li>
 * <li>GET /offers/list - Display all offers (template)</li>
 * <li>GET /offers/all - Get all offers (JSON API)</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
public class OfferController {
    private static final Gson gson = new Gson();
    private static final Map<String, Offer> offers = new ConcurrentHashMap<>();
    private static int offerCounter = 1;
    // Use a writable data folder at runtime so packaged JARs can persist offers
    private static final String OFFERS_FILE = "data/ofertas.json";

    /**
     * Initializes the controller by loading existing offers from ofertas.json.
     * Creates data directory and migrates resource file if needed for backwards compatibility.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public static void initialize() {
        // If old resource file exists and data file doesn't, copy it for backwards compatibility
        java.io.File dataFile = new java.io.File(OFFERS_FILE);
        java.io.File resourceFile = new java.io.File("src/main/resources/ofertas.json");
        if (!dataFile.exists() && resourceFile.exists()) {
            try {
                java.nio.file.Files.createDirectories(dataFile.getParentFile().toPath());
                java.nio.file.Files.copy(resourceFile.toPath(), dataFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Could not migrate offers resource to data folder: " + e.getMessage());
            }
        }

        loadOffersFromFile();
    }

    /**
     * Loads offers from the ofertas.json file
     */
    private static void loadOffersFromFile() {
        java.io.File f = new java.io.File(OFFERS_FILE);
        if (!f.exists()) {
            // nothing to load yet
            return;
        }

        try (FileReader reader = new FileReader(f)) {
            // Support two formats: {"offers": [ ... ]} and a bare JSON array [ ... ]
            Type mapType = new TypeToken<Map<String, List<Offer>>>() {}.getType();
            Map<String, List<Offer>> data = null;

            try {
                data = gson.fromJson(reader, mapType);
            } catch (Exception ex) {
                // try reading as a plain array
            }

            if (data != null && data.containsKey("offers")) {
                List<Offer> offerList = data.get("offers");
                for (Offer offer : offerList) {
                    if (offer.getId() == null || offer.getId().isEmpty()) {
                        offer.setId("offer" + offerCounter++);
                    }
                    offers.put(offer.getId(), offer);
                    // maintain counter if ids like offerN are present
                    bumpCounterFromId(offer.getId());
                }
                return;
            }

            // Reset reader and attempt to parse as array
            try (FileReader reader2 = new FileReader(f)) {
                Type listType = new TypeToken<List<Offer>>() {}.getType();
                List<Offer> offerList = gson.fromJson(reader2, listType);
                if (offerList != null) {
                    for (Offer offer : offerList) {
                        if (offer.getId() == null || offer.getId().isEmpty()) {
                            offer.setId("offer" + offerCounter++);
                        }
                        offers.put(offer.getId(), offer);
                        bumpCounterFromId(offer.getId());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Could not load offers from file: " + e.getMessage());
            // Continue with empty offers map
        }
    }

    /**
     * Saves current offers to the ofertas.json file
     */
    private static void saveOffersToFile() {
        try {
            java.io.File f = new java.io.File(OFFERS_FILE);
            java.io.File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            try (FileWriter writer = new FileWriter(f)) {
                Map<String, List<Offer>> data = new HashMap<>();
                data.put("offers", new ArrayList<>(offers.values()));
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Could not save offers to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns all offers as JSON (API endpoint)
     * GET /api/offers
     */
    public static String getAllOffers(Request req, Response res) {
        res.type("application/json");
        return gson.toJson(new ArrayList<>(offers.values()));
    }

    /**
     * Submits a new offer (API endpoint)
     * POST /api/offers
     * Expects JSON body with: name, email, itemId, amount
     */
    public static String submitOffer(Request req, Response res) {
        res.type("application/json");

        try {
            // Debug: log incoming body to help diagnose malformed JSON
            System.out.println("[OfferController] Incoming body: " + req.body());
            Offer offer = gson.fromJson(req.body(), Offer.class);

            // Validate required fields
            if (offer.getName() == null || offer.getName().trim().isEmpty()) {
                throw new InvalidRequestException("name", "Name is required");
            }
            if (offer.getEmail() == null || offer.getEmail().trim().isEmpty()) {
                throw new InvalidRequestException("email", "Email is required");
            }
            if (!offer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new InvalidRequestException("email", "Invalid email format");
            }
            if (offer.getItemId() == null || offer.getItemId().trim().isEmpty()) {
                throw new InvalidRequestException("itemId", "Item ID is required");
            }
            if (offer.getAmount() <= 0) {
                throw new InvalidRequestException("amount", "Amount must be greater than 0");
            }

            // Verify that the item exists
            verifyItemExists(offer.getItemId());

            // Generate ID and save
            String offerId = "offer" + offerCounter++;
            offer.setId(offerId);
            offers.put(offerId, offer);
            
            // Save to file
            saveOffersToFile();

            res.status(201);
            return gson.toJson(offer);

        } catch (Exception e) {
            if (e instanceof InvalidRequestException || e instanceof ItemNotFoundException) {
                throw e;
            }
            throw new InvalidRequestException("body", "Invalid request body: " + e.getMessage());
        }
    }

    /**
     * Renders the offer form page for a specific item
     * GET /offers/new?itemId=xxx
     */
    public static ModelAndView renderOfferForm(Request req, Response res) {
        String itemId = req.queryParams("itemId");
        
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new InvalidRequestException("itemId", "Item ID is required");
        }

        // Verify item exists
        Item item = getItemById(itemId);

        Map<String, Object> model = new HashMap<>();
        model.put("item", item);
        model.put("itemId", itemId);
        
        return new ModelAndView(model, "offer-form.mustache");
    }

    /**
     * Renders the page showing all submitted offers
     * GET /offers/list
     */
    public static ModelAndView renderOffersList(Request req, Response res) {
        List<Map<String, Object>> offerList = new ArrayList<>();
        
        for (Offer offer : offers.values()) {
            Map<String, Object> offerData = new HashMap<>();
            offerData.put("id", offer.getId());
            offerData.put("name", offer.getName());
            offerData.put("email", offer.getEmail());
            offerData.put("itemId", offer.getItemId());
            offerData.put("amount", offer.getFormattedAmount());
            
            // Get item name if possible
            try {
                Item item = getItemById(offer.getItemId());
                offerData.put("itemName", item.getName());
            } catch (Exception e) {
                offerData.put("itemName", "Unknown Item");
            }
            
            offerList.add(offerData);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("offers", offerList);
        model.put("hasOffers", !offerList.isEmpty());
        
        return new ModelAndView(model, "offers-list.mustache");
    }

    /**
     * Returns a list of offers (as maps) for a given item id suitable for including in template models.
     * This is used by ItemController to display offers related to an item on its detail page.
     *
     * @param itemId the item id to filter offers by
     * @return List of maps containing offer fields (id, name, email, amount)
     */
    public static List<Map<String, Object>> getOffersForItemModel(String itemId) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (itemId == null) return result;

        for (Offer offer : offers.values()) {
            if (itemId.equals(offer.getItemId())) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", offer.getId());
                m.put("name", offer.getName());
                m.put("email", offer.getEmail());
                m.put("amount", offer.getFormattedAmount());
                result.add(m);
            }
        }

        return result;
    }

    /**
     * Helper method to verify an item exists
     */
    private static void verifyItemExists(String itemId) {
        getItemById(itemId); // Will throw ItemNotFoundException if not found
    }

    /**
     * Helper method to get an item by ID
     * This duplicates logic from ItemController but keeps controllers independent
     */
    private static Item getItemById(String id) {
        try (FileReader reader = new FileReader("src/main/resources/items.json")) {
            Type listType = new TypeToken<List<Item>>() {}.getType();
            List<Item> items = gson.fromJson(reader, listType);

            if (items != null) {
                for (Item item : items) {
                    if (item.getId().equals(id)) {
                        return item;
                    }
                }
            }
            throw new ItemNotFoundException(id);
        } catch (IOException e) {
            throw new ItemNotFoundException(id);
        }
    }

    /**
     * If an existing id looks like "offerN" update offerCounter to be > N
     */
    private static void bumpCounterFromId(String id) {
        if (id != null && id.startsWith("offer")) {
            try {
                int n = Integer.parseInt(id.substring(5));
                if (n >= offerCounter) offerCounter = n + 1;
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
