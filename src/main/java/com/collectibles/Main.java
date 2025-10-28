package com.collectibles;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.collectibles.controller.UserController;
import com.collectibles.controller.ItemController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación Spark Collectibles Store
 * Configuración y arranque del servidor
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // Configuración del puerto
        port(4567);

        // Configuración de CORS
        configureCORS();

        // Inicialización de controladores
        UserController userController = new UserController();
        ItemController itemController = new ItemController();

        // Configuración de rutas
        configureRoutes(userController, itemController);

        // Mensaje de inicio
        logger.info("Servidor iniciado en http://localhost:4567");
        System.out.println("===========================================");
        System.out.println("  Spark Collectibles Store API");
        System.out.println("  Servidor corriendo en: http://localhost:4567");
        System.out.println("===========================================");
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
        // Ruta de bienvenida
        get("/", (req, res) -> {
            return gson.toJson(new Response(
                true,
                "Bienvenido a Spark Collectibles Store API - Endpoints: /users, /items",
                null
            ));
        });

        // ========================================
        // GRUPO DE RUTAS: /users
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
        // GRUPO DE RUTAS: /items
        // ========================================
        // Agrupa todas las operaciones relacionadas con artículos coleccionables
        path("/items", () -> {
            // GET /items - Obtener lista de artículos (ID, nombre, precio)
            // Requerimiento 1: Lista de artículos con nombre, precio e ID
            get("", itemController.getAllItems());

            // GET /items/:id - Obtener artículo completo por ID (incluye descripción)
            // Requerimiento 2: Dado un ID, regresar la descripción del artículo
            get("/:id", itemController.getItemById());

            // GET /items/:id/description - Ruta alternativa para obtener solo descripción
            // Subruta específica dentro del grupo /items
            get("/:id/description", itemController.getItemDescription());
        });

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
