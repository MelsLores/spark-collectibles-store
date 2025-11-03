package com.collectibles;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.collectibles.controller.UserController;
import com.collectibles.controller.ItemController;
import com.collectibles.controller.OfferController;
import com.collectibles.model.Item;
import com.collectibles.websocket.PriceUpdateWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main application class for Spark Collectibles Store.
 * Handles server configuration, route setup, and WebSocket initialization.
 * 
 * <p><b>Sprint 3 Features:</b></p>
 * <ul>
 * <li>Item filtering by price range and search term</li>
 * <li>Real-time price updates via WebSocket</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // Inicializar la base de datos PRIMERO
        try {
            logger.info("Initializing database connection pool...");
            com.collectibles.database.DatabaseConfig.initialize();
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            System.err.println("FATAL: Database initialization failed. Please check database configuration.");
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        // Inicializar OfferController
        try {
            logger.info("Initializing OfferController...");
            com.collectibles.controller.OfferController.initialize();
            logger.info("OfferController initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize OfferController", e);
            System.err.println("WARNING: OfferController initialization failed: " + e.getMessage());
        }

        // Configuración del puerto
        port(4567);
        
        // Configurar archivos estáticos (CSS, JS, imágenes) desde /public
        staticFiles.location("/public");

        // Inicialización de controladores
        UserController userController = new UserController();
        ItemController itemController = new ItemController();

        // Initialize template engine
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        
        // Initialize OfferController (loads offers from file)
        OfferController.initialize();

        // Configuración de WebSocket para actualizaciones de precios (Sprint 3)
        // IMPORTANTE: Debe hacerse ANTES de ANY route mapping (including CORS)
        configureWebSocket(itemController);

        // Configuración de CORS
        configureCORS();

        // Configuración de rutas HTTP (API + Views)
        configureRoutes(userController, itemController, templateEngine);

        // Mensaje de inicio
        logger.info("Servidor iniciado en http://localhost:4567");
        logger.info("WebSocket disponible en ws://localhost:4567/ws/prices");
        System.out.println("===========================================");
        System.out.println("  Spark Collectibles Store API");
        System.out.println("  Servidor corriendo en: http://localhost:4567");
        System.out.println("  WebSocket: ws://localhost:4567/ws/prices");
        System.out.println("===========================================");
    }

    /**
     * Configures WebSocket endpoint for real-time price updates.
     * 
     * <p><b>Sprint 3 Feature:</b> Real-time price modification</p>
     * 
     * @param itemController ItemController instance for price updates
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    private static void configureWebSocket(ItemController itemController) {
        // Set ItemController reference in WebSocket handler
        PriceUpdateWebSocket.setItemController(itemController);
        
        // Register WebSocket endpoint
        webSocket("/ws/prices", PriceUpdateWebSocket.class);
        
        logger.info("WebSocket configurado en /ws/prices para actualizaciones de precios");
    }

    /**
     * Configuración de CORS para permitir peticiones desde diferentes orígenes
     */
    private static void configureCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.type("application/json");
        });
    }

    /**
     * Configuración de todas las rutas de la API y vistas HTML
     * 
     * ORGANIZACIÓN DE RUTAS Y GRUPOS:
     * ================================
     * 
     * Esta aplicación utiliza grupos de rutas (path()) para organizar
     * los endpoints de manera lógica y escalable:
     * 
     * 1. GRUPO /users - Gestión de usuarios (UserController)
     *    - Agrupa todas las operaciones CRUD de usuarios
     *    - Permite mantener separada la lógica de usuarios
     * 
     * 2. GRUPO /items - Gestión de artículos coleccionables (ItemController)
     *    - Agrupa todas las operaciones relacionadas con artículos
     *    - Incluye subrutas para información específica (ej: /items/:id/description)
     * 
     * Ventajas de usar grupos de rutas:
     * - Código más organizado y mantenible
     * - Fácil identificación de recursos en la API
     * - Escalabilidad para agregar nuevos endpoints
     * - Separación clara de responsabilidades entre controladores
     */
    private static void configureRoutes(UserController userController, ItemController itemController, 
                                       MustacheTemplateEngine templateEngine) {
        // ========================================
        // RUTA PRINCIPAL - Redireccionar a items
        // ========================================
        get("/", (req, res) -> {
            res.redirect("/items");
            return null;
        });
        
        // Manejar /items directamente (fuera del path() para evitar problemas con trailing slash)
        get("/items", (req, res) -> {
            res.type("text/html; charset=utf-8");
            
            String search = req.queryParams("search");
            String minPriceStr = req.queryParams("minPrice");
            String maxPriceStr = req.queryParams("maxPrice");
            
            Double minPrice = null;
            Double maxPrice = null;
            
            if (minPriceStr != null && !minPriceStr.isEmpty()) {
                try {
                    minPrice = Double.parseDouble(minPriceStr);
                } catch (NumberFormatException e) {
                    // Ignore invalid price
                }
            }
            
            if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(maxPriceStr);
                } catch (NumberFormatException e) {
                    // Ignore invalid price
                }
            }
            
            // Get filtered items
            List<Item> items = itemController.filterItems(search, minPrice, maxPrice);
            
            Map<String, Object> model = new HashMap<>();
            model.put("items", items);
            model.put("hasItems", !items.isEmpty());
            model.put("search", search != null ? search : "");
            model.put("minPrice", minPriceStr != null ? minPriceStr : "");
            model.put("maxPrice", maxPriceStr != null ? maxPriceStr : "");
            
            return templateEngine.render(new ModelAndView(model, "items.mustache"));
        });

        // ========================================
        // GRUPO DE RUTAS: /users (API JSON)
        // ========================================
        // Agrupa todas las operaciones relacionadas con usuarios
        path("/users", () -> {
            // GET /users - Obtener todos los usuarios
            get("", userController.getAllUsers());

            // GET /users/:id - Obtener usuario por ID
            get("/:id", userController.getUserById());

            // POST /users - Crear nuevo usuario
            post("", userController.createUser());

            // PUT /users/:id - Actualizar usuario
            put("/:id", userController.updateUser());

            // DELETE /users/:id - Eliminar usuario
            delete("/:id", userController.deleteUser());

            // OPTIONS /users/:id - Verificar existencia de usuario
            options("/:id", userController.checkUserExists());
        });

        // ========================================
        // GRUPO DE RUTAS: /items (API JSON)
        // ========================================
        path("/items", () -> {
            // HTML VIEW: GET /items/:id/view - Ver detalles de un item (HTML)
            get("/:id/view", (req, res) -> {
                res.type("text/html; charset=utf-8");
                String itemId = req.params(":id");
                
                // Get item details from controller
                Item item = itemController.getItemByIdObject(itemId);
                
                if (item == null) {
                    res.status(404);
                    return "<h1>Item not found</h1>";
                }
                
                // Get offers for this item
                List<Map<String, Object>> offers = OfferController.getOffersForItemModel(itemId);
                
                Map<String, Object> model = new HashMap<>();
                model.put("id", item.getId());
                model.put("name", item.getName());
                model.put("description", item.getDescription());
                model.put("price", item.getPrice());
                model.put("imageUrl", item.getImageUrl());
                model.put("offers", offers);
                model.put("hasOffers", !offers.isEmpty());
                
                return templateEngine.render(new ModelAndView(model, "item-detail.mustache"));
            });
            
            // API JSON: GET /items/api - Obtener lista de artículos en JSON
            get("/api", itemController.getAllItems());

            // API JSON: GET /items/filter - Obtener artículos filtrados (Sprint 3)
            // Soporta: ?search=text&minPrice=100&maxPrice=1000
            get("/filter", itemController.getFilteredItems());

            // API JSON: GET /items/:id - Obtener artículo completo por ID (incluye descripción)
            get("/:id", itemController.getItemById());

            // API JSON: GET /items/:id/description - Ruta alternativa para obtener solo descripción
            get("/:id/description", itemController.getItemDescription());
        });

        // ========================================
        // GRUPO DE RUTAS: /offers
        // ========================================
        path("/offers", () -> {
            // HTML VIEW: GET /offers/new - Renderizar formulario de nueva oferta
            get("/new", (req, res) -> {
                res.type("text/html; charset=utf-8");
                return templateEngine.render(OfferController.renderOfferForm(req, res));
            });
            
            // HTML VIEW: GET /offers/list - Renderizar lista de ofertas
            get("/list", (req, res) -> {
                res.type("text/html; charset=utf-8");
                return templateEngine.render(OfferController.renderOffersList(req, res));
            });
            
            // API JSON: POST /offers - Crear nueva oferta
            post("", OfferController::submitOffer);
            
            // API JSON: GET /offers/all - Obtener todas las ofertas
            get("/all", OfferController::getAllOffers);
        });
        
        // API route for offers (alternative path for compatibility)
        post("/api/offers", OfferController::submitOffer);

        // Manejo de rutas no encontradas
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new Response(
                false,
                "Ruta no encontrada: " + req.pathInfo(),
                null
            ));
        });

        // Manejo de errores internos
        internalServerError((req, res) -> {
            res.type("application/json");
            return gson.toJson(new Response(
                false,
                "Error interno del servidor",
                null
            ));
        });

        // Manejo de errores internos
        internalServerError((req, res) -> {
            res.type("application/json");
            return gson.toJson(new Response(
                false,
                "Error interno del servidor",
                null
            ));
        });
    }

    /**
     * Clase interna para respuestas estandarizadas
     */
    static class Response {
        private boolean success;
        private String message;
        private Object data;

        public Response(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}
