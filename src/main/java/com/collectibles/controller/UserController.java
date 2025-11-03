package com.collectibles.controller;

import com.collectibles.model.User;
import com.google.gson.Gson;
import spark.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for user management operations.
 * Implements all CRUD operations for User entities.
 * 
 * <p><b>REST API Endpoints:</b></p>
 * <ul>
 * <li>GET /users - Retrieve all users</li>
 * <li>GET /users/:id - Retrieve user by ID</li>
 * <li>POST /users - Create new user</li>
 * <li>PUT /users/:id - Update existing user</li>
 * <li>DELETE /users/:id - Delete user</li>
 * <li>OPTIONS /users/:id - Check if user exists</li>
 * </ul>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final Gson gson = new Gson();
    private final Map<String, User> userDatabase = new HashMap<>();

    public UserController() {
        // Datos de prueba
        initializeSampleData();
    }

    /**
     * Initializes sample user data for testing purposes.
     * Creates three default users: admin, seller, and buyer.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    private void initializeSampleData() {
        User user1 = new User("1", "Rafael García", "rafael@collectibles.com", "admin");
        User user2 = new User("2", "Ramón Torres", "ramon@collectibles.com", "seller");
        User user3 = new User("3", "Sofía Mendez", "sofia@collectibles.com", "buyer");

        userDatabase.put("1", user1);
        userDatabase.put("2", user2);
        userDatabase.put("3", user3);

        logger.info("Datos de ejemplo inicializados: {} usuarios", userDatabase.size());
    }

    /**
     * Route handler for retrieving all users.
     * Returns a list of all users in the database.
     * 
     * <p><b>Endpoint:</b> GET /users</p>
     * 
     * <p><b>Response Example:</b></p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Usuarios obtenidos exitosamente",
     *   "data": [...]
     * }
     * </pre>
     * 
     * @return Route handler that processes GET /users requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route getAllUsers() {
        return (req, res) -> {
            logger.info("GET /users - Obteniendo todos los usuarios");
            List<User> users = new ArrayList<>(userDatabase.values());
            
            ApiResponse response = new ApiResponse(
                true,
                "Usuarios obtenidos exitosamente",
                users
            );
            
            res.type("application/json");
            res.status(200);
            return gson.toJson(response);
        };
    }

    /**
     * Route handler for retrieving a user by ID.
     * 
     * <p><b>Endpoint:</b> GET /users/:id</p>
     * 
     * <p><b>Path Parameters:</b></p>
     * <ul>
     * <li>id - The user ID to retrieve</li>
     * </ul>
     * 
     * <p><b>Response Codes:</b></p>
     * <ul>
     * <li>200 - User found and returned</li>
     * <li>404 - User not found</li>
     * </ul>
     * 
     * @return Route handler that processes GET /users/:id requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route getUserById() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("GET /users/{} - Obteniendo usuario por ID", id);

            User user = userDatabase.get(id);
            
            if (user != null) {
                ApiResponse response = new ApiResponse(
                    true,
                    "Usuario encontrado",
                    user
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
            } else {
                ApiResponse response = new ApiResponse(
                    false,
                    "Usuario no encontrado",
                    null
                );
                res.type("application/json");
                res.status(404);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Route handler for creating a new user.
     * 
     * <p><b>Endpoint:</b> POST /users</p>
     * 
     * <p><b>Request Body:</b> User JSON object</p>
     * <pre>
     * {
     *   "id": "4",
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "role": "buyer"
     * }
     * </pre>
     * 
     * <p><b>Response Codes:</b></p>
     * <ul>
     * <li>201 - User created successfully</li>
     * <li>400 - Invalid request (missing ID or user already exists)</li>
     * </ul>
     * 
     * @return Route handler that processes POST /users requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route createUser() {
        return (req, res) -> {
            logger.info("POST /users - Creando nuevo usuario");
            
            try {
                User newUser = gson.fromJson(req.body(), User.class);
                
                // Validación básica
                if (newUser.getId() == null || newUser.getId().isEmpty()) {
                    newUser.setId(String.valueOf(userDatabase.size() + 1));
                }
                
                if (newUser.getName() == null || newUser.getName().isEmpty()) {
                    ApiResponse response = new ApiResponse(
                        false,
                        "El nombre es requerido",
                        null
                    );
                    res.type("application/json");
                    res.status(400);
                    return gson.toJson(response);
                }

                newUser.setCreatedAt(java.time.LocalDateTime.now().toString());
                userDatabase.put(newUser.getId(), newUser);
                
                logger.info("Usuario creado exitosamente: {}", newUser.getId());
                
                ApiResponse response = new ApiResponse(
                    true,
                    "Usuario creado exitosamente",
                    newUser
                );
                res.type("application/json");
                res.status(201);
                return gson.toJson(response);
                
            } catch (Exception e) {
                logger.error("Error al crear usuario: ", e);
                ApiResponse response = new ApiResponse(
                    false,
                    "Error al crear usuario: " + e.getMessage(),
                    null
                );
                res.type("application/json");
                res.status(400);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Route handler for updating an existing user.
     * 
     * <p><b>Endpoint:</b> PUT /users/:id</p>
     * 
     * <p><b>Path Parameters:</b></p>
     * <ul>
     * <li>id - The user ID to update</li>
     * </ul>
     * 
     * <p><b>Request Body:</b> User JSON object with updated fields</p>
     * 
     * <p><b>Response Codes:</b></p>
     * <ul>
     * <li>200 - User updated successfully</li>
     * <li>404 - User not found</li>
     * <li>400 - Invalid request body</li>
     * </ul>
     * 
     * @return Route handler that processes PUT /users/:id requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route updateUser() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("PUT /users/{} - Actualizando usuario", id);

            User existingUser = userDatabase.get(id);
            
            if (existingUser == null) {
                ApiResponse response = new ApiResponse(
                    false,
                    "Usuario no encontrado",
                    null
                );
                res.type("application/json");
                res.status(404);
                return gson.toJson(response);
            }

            try {
                User updatedUser = gson.fromJson(req.body(), User.class);
                updatedUser.setId(id);
                updatedUser.setCreatedAt(existingUser.getCreatedAt());
                
                userDatabase.put(id, updatedUser);
                
                logger.info("Usuario actualizado exitosamente: {}", id);
                
                ApiResponse response = new ApiResponse(
                    true,
                    "Usuario actualizado exitosamente",
                    updatedUser
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
                
            } catch (Exception e) {
                logger.error("Error al actualizar usuario: ", e);
                ApiResponse response = new ApiResponse(
                    false,
                    "Error al actualizar usuario: " + e.getMessage(),
                    null
                );
                res.type("application/json");
                res.status(400);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Route handler for deleting a user.
     * 
     * <p><b>Endpoint:</b> DELETE /users/:id</p>
     * 
     * <p><b>Path Parameters:</b></p>
     * <ul>
     * <li>id - The user ID to delete</li>
     * </ul>
     * 
     * <p><b>Response Codes:</b></p>
     * <ul>
     * <li>200 - User deleted successfully</li>
     * <li>404 - User not found</li>
     * </ul>
     * 
     * @return Route handler that processes DELETE /users/:id requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route deleteUser() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("DELETE /users/{} - Eliminando usuario", id);

            User deletedUser = userDatabase.remove(id);
            
            if (deletedUser != null) {
                logger.info("Usuario eliminado exitosamente: {}", id);
                
                ApiResponse response = new ApiResponse(
                    true,
                    "Usuario eliminado exitosamente",
                    deletedUser
                );
                res.type("application/json");
                res.status(200);
                return gson.toJson(response);
            } else {
                ApiResponse response = new ApiResponse(
                    false,
                    "Usuario no encontrado",
                    null
                );
                res.type("application/json");
                res.status(404);
                return gson.toJson(response);
            }
        };
    }

    /**
     * Route handler for checking if a user exists.
     * 
     * <p><b>Endpoint:</b> OPTIONS /users/:id</p>
     * 
     * <p><b>Path Parameters:</b></p>
     * <ul>
     * <li>id - The user ID to check</li>
     * </ul>
     * 
     * <p><b>Response:</b> Returns exists boolean flag</p>
     * <pre>
     * {
     *   "success": true,
     *   "message": "Usuario existe",
     *   "data": {
     *     "exists": true,
     *     "id": "1"
     *   }
     * }
     * </pre>
     * 
     * @return Route handler that processes OPTIONS /users/:id requests
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public Route checkUserExists() {
        return (req, res) -> {
            String id = req.params(":id");
            logger.info("OPTIONS /users/{} - Verificando existencia de usuario", id);

            boolean exists = userDatabase.containsKey(id);
            
            ApiResponse response = new ApiResponse(
                true,
                exists ? "Usuario existe" : "Usuario no existe",
                Map.of("exists", exists, "id", id)
            );
            
            res.type("application/json");
            res.status(200);
            return gson.toJson(response);
        };
    }

    /**
     * Standard API response wrapper class.
     * Provides consistent response format across all endpoints.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    private static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}
