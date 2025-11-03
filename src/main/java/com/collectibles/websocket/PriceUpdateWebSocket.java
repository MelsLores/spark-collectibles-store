package com.collectibles.websocket;

import com.collectibles.controller.ItemController;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time price updates.
 * 
 * <p><b>Sprint 3 Feature:</b> Real-time price modification using WebSocket</p>
 * 
 * <p>This WebSocket endpoint allows clients to receive instant notifications
 * when item prices are updated. It maintains a registry of connected clients
 * and broadcasts price changes to all active sessions.</p>
 * 
 * <p><b>WebSocket Endpoint:</b> ws://localhost:4567/ws/prices</p>
 * 
 * <p><b>Message Format (Client to Server):</b></p>
 * <pre>
 * {
 *   "itemId": "item1",
 *   "newPrice": 899.99
 * }
 * </pre>
 * 
 * <p><b>Broadcast Format (Server to All Clients):</b></p>
 * <pre>
 * {
 *   "type": "PRICE_UPDATE",
 *   "itemId": "item1",
 *   "newPrice": "$899.99 USD",
 *   "timestamp": 1234567890
 * }
 * </pre>
 * 
 * @author Melany Rivera
 * @author Ricardo Ruiz
 * @version 3.0
 * @since 02/11/2025
 */
@WebSocket
public class PriceUpdateWebSocket {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceUpdateWebSocket.class);
    private static final Gson gson = new Gson();
    
    /**
     * Thread-safe registry of all active WebSocket sessions.
     * Key: Session ID, Value: Session object
     */
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Reference to ItemController for updating item prices.
     */
    private static ItemController itemController;
    
    /**
     * Sets the ItemController instance for price updates.
     * Must be called during application initialization.
     * 
     * @param controller The ItemController instance
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public static void setItemController(ItemController controller) {
        itemController = controller;
        logger.info("ItemController set for WebSocket price updates");
    }
    
    /**
     * Called when a client connects to the WebSocket endpoint.
     * Registers the session and sends a welcome message.
     * 
     * @param session The WebSocket session that connected
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        String sessionId = session.getRemoteAddress().toString();
        activeSessions.put(sessionId, session);
        logger.info("WebSocket client connected: {} (Total clients: {})", 
                    sessionId, activeSessions.size());
        
        // Send welcome message
        try {
            Map<String, Object> welcome = new HashMap<>();
            welcome.put("type", "CONNECTED");
            welcome.put("message", "Connected to price update service");
            welcome.put("clientCount", activeSessions.size());
            session.getRemote().sendString(gson.toJson(welcome));
        } catch (IOException e) {
            logger.error("Failed to send welcome message", e);
        }
    }
    
    /**
     * Called when a client disconnects from the WebSocket endpoint.
     * Removes the session from the active registry.
     * 
     * @param session The WebSocket session that disconnected
     * @param statusCode The close status code
     * @param reason The close reason
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String sessionId = session.getRemoteAddress().toString();
        activeSessions.remove(sessionId);
        logger.info("WebSocket client disconnected: {} - Status: {} - Reason: {} (Remaining: {})", 
                    sessionId, statusCode, reason, activeSessions.size());
    }
    
    /**
     * Called when a message is received from a client.
     * Processes price update requests and broadcasts changes to all clients.
     * 
     * <p><b>Expected Message Format:</b></p>
     * <pre>
     * {
     *   "itemId": "item1",
     *   "newPrice": 899.99
     * }
     * </pre>
     * 
     * @param session The session that sent the message
     * @param message The message content (JSON string)
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.info("Received message from {}: {}", session.getRemoteAddress(), message);
        
        try {
            // Parse price update request
            PriceUpdateRequest request = gson.fromJson(message, PriceUpdateRequest.class);
            
            if (request == null || request.itemId == null || request.newPrice <= 0) {
                sendError(session, "Invalid price update request");
                return;
            }
            
            // Update item price
            if (itemController == null) {
                sendError(session, "Price update service not initialized");
                return;
            }
            
            boolean updated = itemController.updateItemPrice(request.itemId, request.newPrice);
            
            if (updated) {
                // Broadcast update to all clients
                broadcastPriceUpdate(request.itemId, request.newPrice);
                logger.info("Price updated and broadcasted: {} -> {}", 
                           request.itemId, request.newPrice);
            } else {
                sendError(session, "Failed to update price: item not found");
            }
            
        } catch (SQLException e) {
            logger.error("Database error updating price", e);
            sendError(session, "Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing price update", e);
            sendError(session, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Called when an error occurs on a WebSocket session.
     * 
     * @param session The session that encountered the error
     * @param error The error that occurred
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket error for {}: {}", 
                    session.getRemoteAddress(), error.getMessage(), error);
    }
    
    /**
     * Broadcasts a price update message to all connected WebSocket clients.
     * 
     * @param itemId The ID of the item that was updated
     * @param newPrice The new price value
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public static void broadcastPriceUpdate(String itemId, double newPrice) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "PRICE_UPDATE");
        update.put("itemId", itemId);
        update.put("newPrice", String.format("$%.2f USD", newPrice));
        update.put("priceNumeric", newPrice);
        update.put("timestamp", System.currentTimeMillis());
        
        String jsonMessage = gson.toJson(update);
        
        logger.info("Broadcasting price update to {} clients: {}", 
                   activeSessions.size(), jsonMessage);
        
        // Send to all connected clients
        activeSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(jsonMessage);
                }
            } catch (IOException e) {
                logger.error("Failed to send update to client: {}", 
                           session.getRemoteAddress(), e);
            }
        });
    }
    
    /**
     * Sends an error message to a specific client session.
     * 
     * @param session The session to send the error to
     * @param errorMessage The error message
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    private void sendError(Session session, String errorMessage) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "ERROR");
            error.put("message", errorMessage);
            error.put("timestamp", System.currentTimeMillis());
            
            if (session.isOpen()) {
                session.getRemote().sendString(gson.toJson(error));
            }
        } catch (IOException e) {
            logger.error("Failed to send error message", e);
        }
    }
    
    /**
     * Gets the count of currently connected WebSocket clients.
     * 
     * @return Number of active sessions
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Internal class for parsing price update requests from clients.
     * 
     * @author Melany Rivera
     * @author Ricardo Ruiz
     * @since 02/11/2025
     */
    private static class PriceUpdateRequest {
        String itemId;
        double newPrice;
    }
}
