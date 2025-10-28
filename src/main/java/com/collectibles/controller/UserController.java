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
 * Controlador para gestión de usuarios
 * Implementa todas las operaciones CRUD
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
     * Inicializa datos de ejemplo
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
     * GET /users - Obtener todos los usuarios
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
     * GET /users/:id - Obtener usuario por ID
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
     * POST /users - Crear nuevo usuario
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
     * PUT /users/:id - Actualizar usuario existente
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
     * DELETE /users/:id - Eliminar usuario
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
     * OPTIONS /users/:id - Verificar si existe un usuario
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
     * Clase interna para respuestas estandarizadas de la API
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
