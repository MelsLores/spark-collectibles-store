package com.collectibles.controller;

import com.collectibles.database.DatabaseConfig;
import com.collectibles.model.User;
import com.google.gson.Gson;
import spark.Route;
import spark.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de usuarios
 * Implementa todas las operaciones CRUD con PostgreSQL
 */
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final Gson gson = new Gson();

    /**
     * GET /users - Obtener todos los usuarios
     */
    public Route getAllUsers() {
        return (req, res) -> {
            logger.info("GET /users - Obteniendo todos los usuarios");
            List<User> users = new ArrayList<>();
            
            String sql = "SELECT id, name, email, role, created_at FROM users ORDER BY created_at DESC";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    User user = new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt.toLocalDateTime().toString());
                    }
                    users.add(user);
                }
                
                logger.info("Se encontraron {} usuarios", users.size());
                
            } catch (SQLException e) {
                logger.error("Error al obtener usuarios: ", e);
                ApiResponse response = new ApiResponse(
                    false,
                    "Error al obtener usuarios: " + e.getMessage(),
                    null
                );
                res.type("application/json");
                res.status(500);
                return gson.toJson(response);
            }
            
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

            String sql = "SELECT id, name, email, role, created_at FROM users WHERE id = ?";
            User user = null;
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        user = new User(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                        );
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (createdAt != null) {
                            user.setCreatedAt(createdAt.toLocalDateTime().toString());
                        }
                    }
                }
                
            } catch (SQLException e) {
                logger.error("Error al obtener usuario: ", e);
                ApiResponse response = new ApiResponse(
                    false,
                    "Error al obtener usuario: " + e.getMessage(),
                    null
                );
                res.type("application/json");
                res.status(500);
                return gson.toJson(response);
            }
            
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
                
                if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
                    ApiResponse response = new ApiResponse(
                        false,
                        "El email es requerido",
                        null
                    );
                    res.type("application/json");
                    res.status(400);
                    return gson.toJson(response);
                }
                
                // Generate ID if not provided
                if (newUser.getId() == null || newUser.getId().isEmpty()) {
                    newUser.setId(java.util.UUID.randomUUID().toString());
                }
                
                String sql = "INSERT INTO users (id, name, email, role, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, newUser.getId());
                    pstmt.setString(2, newUser.getName());
                    pstmt.setString(3, newUser.getEmail());
                    pstmt.setString(4, newUser.getRole() != null ? newUser.getRole() : "buyer");
                    
                    pstmt.executeUpdate();
                    
                    logger.info("Usuario creado exitosamente: {}", newUser.getId());
                    
                    ApiResponse response = new ApiResponse(
                        true,
                        "Usuario creado exitosamente",
                        newUser
                    );
                    res.type("application/json");
                    res.status(201);
                    return gson.toJson(response);
                    
                } catch (SQLException e) {
                    logger.error("Error al crear usuario en BD: ", e);
                    ApiResponse response = new ApiResponse(
                        false,
                        "Error al crear usuario: " + e.getMessage(),
                        null
                    );
                    res.type("application/json");
                    res.status(500);
                    return gson.toJson(response);
                }
                
            } catch (Exception e) {
                logger.error("Error al procesar solicitud: ", e);
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

            try {
                User updatedUser = gson.fromJson(req.body(), User.class);
                
                String sql = "UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?";
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, updatedUser.getName());
                    pstmt.setString(2, updatedUser.getEmail());
                    pstmt.setString(3, updatedUser.getRole());
                    pstmt.setString(4, id);
                    
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        ApiResponse response = new ApiResponse(
                            false,
                            "Usuario no encontrado",
                            null
                        );
                        res.type("application/json");
                        res.status(404);
                        return gson.toJson(response);
                    }
                    
                    updatedUser.setId(id);
                    logger.info("Usuario actualizado exitosamente: {}", id);
                    
                    ApiResponse response = new ApiResponse(
                        true,
                        "Usuario actualizado exitosamente",
                        updatedUser
                    );
                    res.type("application/json");
                    res.status(200);
                    return gson.toJson(response);
                    
                } catch (SQLException e) {
                    logger.error("Error al actualizar usuario: ", e);
                    ApiResponse response = new ApiResponse(
                        false,
                        "Error al actualizar usuario: " + e.getMessage(),
                        null
                    );
                    res.type("application/json");
                    res.status(500);
                    return gson.toJson(response);
                }
                
            } catch (Exception e) {
                logger.error("Error al procesar solicitud: ", e);
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

            String sql = "DELETE FROM users WHERE id = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Usuario eliminado exitosamente: {}", id);
                    
                    ApiResponse response = new ApiResponse(
                        true,
                        "Usuario eliminado exitosamente",
                        Map.of("id", id)
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
                
            } catch (SQLException e) {
                logger.error("Error al eliminar usuario: ", e);
                ApiResponse response = new ApiResponse(
                    false,
                    "Error al eliminar usuario: " + e.getMessage(),
                    null
                );
                res.type("application/json");
                res.status(500);
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

            String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
            boolean exists = false;
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        exists = rs.getInt(1) > 0;
                    }
                }
                
            } catch (SQLException e) {
                logger.error("Error al verificar usuario: ", e);
            }
            
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
     * Render users list page (HTML template)
     */
    public ModelAndView renderUsersListPage() {
        logger.info("Rendering users list page");
        List<User> users = new ArrayList<>();
        
        String sql = "SELECT id, name, email, role, created_at FROM users ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role")
                );
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime().toString());
                }
                users.add(user);
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener usuarios para renderizar: ", e);
        }
        
        // Add role flags for each user
        List<Map<String, Object>> usersWithFlags = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("createdAt", user.getCreatedAt());
            userMap.put("isAdmin", "admin".equalsIgnoreCase(user.getRole()));
            userMap.put("isSeller", "seller".equalsIgnoreCase(user.getRole()));
            userMap.put("isBuyer", "buyer".equalsIgnoreCase(user.getRole()));
            usersWithFlags.add(userMap);
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("users", usersWithFlags);
        model.put("totalUsers", users.size());
        model.put("hasUsers", !users.isEmpty());
        
        // Statistics
        long adminCount = users.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long sellerCount = users.stream().filter(u -> "seller".equalsIgnoreCase(u.getRole())).count();
        long buyerCount = users.stream().filter(u -> "buyer".equalsIgnoreCase(u.getRole())).count();
        
        model.put("adminCount", adminCount);
        model.put("sellerCount", sellerCount);
        model.put("buyerCount", buyerCount);
        
        return new ModelAndView(model, "users.mustache");
    }

    /**
     * Render user detail page (HTML template)
     */
    public ModelAndView renderUserDetailPage(String id) {
        logger.info("Rendering user detail page for ID: {}", id);
        User user = null;
        
        String sql = "SELECT id, name, email, role, created_at FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt.toLocalDateTime().toString());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener usuario para renderizar: ", e);
        }
        
        if (user == null) {
            throw new com.collectibles.exception.UserNotFoundException("user", id);
        }
        
        Map<String, Object> model = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("isAdmin", "admin".equalsIgnoreCase(user.getRole()));
        userMap.put("isSeller", "seller".equalsIgnoreCase(user.getRole()));
        userMap.put("isBuyer", "buyer".equalsIgnoreCase(user.getRole()));
        
        model.put("user", userMap);
        
        return new ModelAndView(model, "user-detail.mustache");
    }

    /**
     * Render user form page (HTML template)
     */
    public ModelAndView renderUserForm(String id) {
        logger.info("Rendering user form for ID: {}", id);
        Map<String, Object> model = new HashMap<>();
        
        if (id != null && !id.isEmpty() && !"new".equals(id)) {
            String sql = "SELECT id, name, email, role, created_at FROM users WHERE id = ?";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                        );
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (createdAt != null) {
                            user.setCreatedAt(createdAt.toLocalDateTime().toString());
                        }
                        model.put("user", user);
                        model.put("isEdit", true);
                    }
                }
                
            } catch (SQLException e) {
                logger.error("Error al obtener usuario para editar: ", e);
            }
        } else {
            model.put("isEdit", false);
        }
        
        return new ModelAndView(model, "user-form.mustache");
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
