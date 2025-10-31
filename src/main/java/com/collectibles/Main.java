package com.collectibles;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.collectibles.controller.UserController;
import com.collectibles.controller.ItemController;
import com.collectibles.controller.OfferController;
import com.collectibles.database.DatabaseConfig;
import com.collectibles.exception.*;
import spark.template.mustache.MustacheTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación Spark Collectibles Store
 * Configuración y arranque del servidor
 * 
 * Sprint 2: Enhanced with centralized exception handling and Mustache templates
 * Updated: PostgreSQL database integration
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Gson gson = new Gson();
    // Logical -> actual resource filename mapping. Allows using friendly names while
    // serving the existing "item N.jpg" files without duplicating binaries.
    private static final Map<String, String> IMAGE_ALIAS = createImageAlias();

    private static Map<String, String> createImageAlias() {
        Map<String, String> m = new HashMap<>();
        m.put("rosalia.jpg", "item 1.jpg");
        m.put("peso pluma.jpg", "item 2.jpg");
        m.put("coldplay.jpg", "item 3.jpg");
        m.put("guitarra.jpg", "item 4.jpg");
        m.put("bad bunny.jpg", "item 5.jpg");
        m.put("cardi b.jpg", "item 6.jpg");
        m.put("snoop dogg.jpg", "item 7.jpg");
        return m;
    }

    public static void main(String[] args) {
        // Inicializar base de datos PostgreSQL
        logger.info("Inicializando conexión a base de datos PostgreSQL...");
    DatabaseConfig.initialize();
    // Seed items table from items.json so the DB contains exactly the items from the JSON
    DatabaseConfig.seedItemsFromJson();
    // Initialize sample users/offers only where missing (items insertion will be skipped if already present)
    DatabaseConfig.initializeSampleData();
        
        // Shutdown hook para cerrar conexiones
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Cerrando conexiones de base de datos...");
            DatabaseConfig.close();
        }));
        
        // Configuración del puerto
        port(4567);

        // Configuración de archivos estáticos (Sprint 2)
        staticFiles.location("/public");

        // Serve product images from classpath /products
        // Example: /products/rosalia.jpg  -> serves existing /products/item 1.jpg
        get("/products/:name", (req, res) -> {
            String name = req.params(":name");
            // decode URL-encoded characters into actual characters for resource lookup
            String decoded = URLDecoder.decode(name, StandardCharsets.UTF_8);
            // map friendly logical name to actual resource filename if alias exists
            String actual = IMAGE_ALIAS.getOrDefault(decoded, decoded);
            String resourceName = "/products/" + actual.replace("%20", " ");
            var is = Main.class.getResourceAsStream(resourceName);
            if (is == null) {
                res.status(404);
                return "";
            }
            // Determine content type by extension (no WebP support per user request)
            String lower = decoded.toLowerCase();
            if (lower.endsWith(".png")) {
                res.type("image/png");
            } else {
                // Default to jpeg for .jpg/.jpeg and unknown extensions
                res.type("image/jpeg");
            }

            try (is) {
                byte[] bytes = is.readAllBytes();
                res.raw().getOutputStream().write(bytes);
                res.raw().getOutputStream().flush();
            }
            return res.raw();
        });

        // Configuración de CORS
        configureCORS();

        // Configuración de manejo de excepciones (Sprint 2)
        configureExceptionHandling();

        // Inicialización de controladores
        UserController userController = new UserController();
        ItemController itemController = new ItemController();
        OfferController.initialize();  // Initialize offers from file

        // Configuración de rutas
        configureRoutes(userController, itemController);
        
        // Ruta de prueba para error 500 (Sprint 2 Testing)
        get("/test/error500", (req, res) -> {
            throw new ServerException("test", "This is a test of 500 error handling");
        });

        // Mensaje de inicio
        logger.info("Servidor iniciado en http://localhost:4567");
        System.out.println("===========================================");
        System.out.println("  Spark Collectibles Store API");
        System.out.println("  Sprint 2: Exception Handling Enabled");
        System.out.println("  Servidor corriendo en: http://localhost:4567");
        System.out.println("===========================================");
    }

    /**
     * Configuración centralizada de manejo de excepciones (Sprint 2)
     * 
     * Casos soportados:
     * - 404 Not Found: ItemNotFoundException, UserNotFoundException
     * - 400 Bad Request: InvalidRequestException
     * - 500 Internal Server Error: ServerException, Exception genérica
     */
    private static void configureExceptionHandling() {
        // Manejo de ItemNotFoundException (404)
        exception(ItemNotFoundException.class, (ex, req, res) -> {
            String response = ExceptionHandler.handleItemNotFound(ex, req, res);
            res.body(response);
        });

        // Manejo de UserNotFoundException (404)
        exception(UserNotFoundException.class, (ex, req, res) -> {
            String response = ExceptionHandler.handleUserNotFound(ex, req, res);
            res.body(response);
        });

        // Manejo de InvalidRequestException (400)
        exception(InvalidRequestException.class, (ex, req, res) -> {
            String response = ExceptionHandler.handleInvalidRequest(ex, req, res);
            res.body(response);
        });

        // Manejo de ServerException (500)
        exception(ServerException.class, (ex, req, res) -> {
            String response = ExceptionHandler.handleServerException(ex, req, res);
            res.body(response);
        });

        // Manejo de excepciones genéricas (500)
        exception(Exception.class, (ex, req, res) -> {
            String response = ExceptionHandler.handleGenericException(ex, req, res);
            res.body(response);
        });

        logger.info("Exception handling configured for: 404, 400, 500 errors");
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
            // Only set JSON content type for API routes
            if (request.pathInfo().startsWith("/api/") || 
                request.pathInfo().equals("/")) {
                response.type("application/json");
            }
        });
    }

    /**
     * Configuración de todas las rutas de la API
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
    private static void configureRoutes(UserController userController, ItemController itemController) {
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        
        // ========================================
        // TEMPLATE ROUTES (Sprint 2 - Web UI)
        // ========================================
        // HTML pages using Mustache templates
        
        // GET /users - Render users list page (HTML)
        get("/users", "text/html", (req, res) -> {
            return userController.renderUsersListPage();
        }, templateEngine);
        
        // GET /users/:id - Render user detail page (HTML)
        get("/users/:id", "text/html", (req, res) -> {
            String id = req.params(":id");
            // If it's "form" segment, skip to form route
            if ("form".equals(id)) {
                return null;
            }
            return userController.renderUserDetailPage(id);
        }, templateEngine);
        
        // GET /users/form/new - Render new user form page (HTML)
        get("/users/form/new", "text/html", (req, res) -> {
            return userController.renderUserForm(null);
        }, templateEngine);
        
        // GET /users/form/:id - Render edit user form page (HTML)
        get("/users/form/:id", "text/html", (req, res) -> {
            String id = req.params(":id");
            return userController.renderUserForm(id);
        }, templateEngine);
        
        // GET /items - Render items list page (HTML)
        get("/items", "text/html", (req, res) -> {
            return itemController.renderItemsListPage();
        }, templateEngine);
        
        // GET /items/:id - Render item detail page (HTML)
        get("/items/:id", "text/html", (req, res) -> {
            String id = req.params(":id");
            return itemController.renderItemDetailPage(id);
        }, templateEngine);
        
        // GET /offers/new?itemId=xxx - Render offer form page (HTML)
        get("/offers/new", "text/html", (req, res) -> {
            return OfferController.renderOfferForm(req, res);
        }, templateEngine);
        
        // GET /offers - Render offers list page (HTML)
        get("/offers", "text/html", (req, res) -> {
            return OfferController.renderOffersList(req, res);
        }, templateEngine);
        
        // GET /offers/list - Alias for /offers (backward compatibility)
        get("/offers/list", "text/html", (req, res) -> {
            return OfferController.renderOffersList(req, res);
        }, templateEngine);
        
        // ========================================
        // API ROUTES (JSON endpoints)
        // ========================================
        
        // API: GET /api/users - Get all users as JSON
        get("/api/users", userController.getAllUsers());
        
        // API: GET /api/users/:id - Get user by ID as JSON
        get("/api/users/:id", userController.getUserById());
        
        // API: POST /api/users - Create user as JSON
        post("/api/users", userController.createUser());
        
        // API: PUT /api/users/:id - Update user as JSON
        put("/api/users/:id", userController.updateUser());
        
        // API: DELETE /api/users/:id - Delete user as JSON
        delete("/api/users/:id", userController.deleteUser());
        
        // API: GET /api/items - Get all items as JSON
        get("/api/items", itemController.getAllItems());
        
        // API: GET /api/items/:id - Get item by ID as JSON
        get("/api/items/:id", itemController.getItemById());
        
        // API: GET /api/items/:id/description - Get item description as JSON
        get("/api/items/:id/description", itemController.getItemDescription());
        
        // API: GET /api/offers - Get all offers as JSON
        get("/api/offers", OfferController::getAllOffers);
        
        // API: POST /api/offers - Submit new offer as JSON
        post("/api/offers", OfferController::submitOffer);
        
        // Home page (landing) - render index.mustache with featured items
        get("/", "text/html", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // Take first 3 items as featured
            var all = itemController.getItemsForTemplate();
            List<Map<String, Object>> featured = new java.util.ArrayList<>();
            for (int i = 0; i < Math.min(3, all.size()); i++) {
                Map<String, Object> it = all.get(i);
                // mark first as active for carousel
                it.put("first", i == 0);
                featured.add(it);
            }
            model.put("featured", featured);
            // Return ModelAndView — template engine will render it (do not pre-render here)
            return new spark.ModelAndView(model, "index.mustache");
        }, templateEngine);

        // Manejo de rutas no encontradas
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new Response(
                false,
                "Ruta no encontrada",
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
