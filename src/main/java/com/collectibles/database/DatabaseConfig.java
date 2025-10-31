package com.collectibles.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Database configuration and connection pool management using HikariCP
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    // Database configuration - ajusta estos valores según tu configuración
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/collectibles_db");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "postgres");

    /**
     * Initialize the database connection pool and create tables
     */
    public static void initialize() {
        logger.info("Initializing database connection pool...");
        // First attempt: try to create pool and tables normally
        try {
            setupDataSource(DB_URL);
            logger.info("Database initialized successfully");
            return;
        } catch (RuntimeException re) {
            // Walk the cause chain to find an underlying SQLException (or message indicating missing DB)
            Throwable cause = re;
            SQLException foundSqlEx = null;
            while (cause != null) {
                if (cause instanceof SQLException) {
                    foundSqlEx = (SQLException) cause;
                    break;
                }
                cause = cause.getCause();
            }

            String message = re.getMessage() != null ? re.getMessage().toLowerCase() : "";
            if (foundSqlEx != null) {
                String sqlState = foundSqlEx.getSQLState();
                message = foundSqlEx.getMessage() != null ? foundSqlEx.getMessage().toLowerCase() : message;
                // SQLState 3D000 = invalid_catalog_name (database does not exist)
                if ("3d000".equalsIgnoreCase(sqlState) || message.contains("does not exist") || message.contains("no existe la base de datos")) {
                    logger.warn("Target database appears to be missing ({}). Attempting to create it using maintenance DB...", DB_URL);
                    try {
                        createDatabaseUsingMaintenanceDb();
                        // retry setup
                        setupDataSource(DB_URL);
                        logger.info("Database created and initialized successfully");
                        return;
                    } catch (SQLException | RuntimeException e) {
                        logger.error("Failed to create or initialize database automatically: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to initialize database and automatic creation failed", e);
                    }
                }
            } else if (message.contains("does not exist") || message.contains("no existe la base de datos")) {
                // Try to create DB even if SQLException wasn't caught explicitly
                logger.warn("Target database appears to be missing (detected by message). Attempting to create it using maintenance DB...", DB_URL);
                try {
                    createDatabaseUsingMaintenanceDb();
                    setupDataSource(DB_URL);
                    logger.info("Database created and initialized successfully");
                    return;
                } catch (SQLException | RuntimeException e) {
                    logger.error("Failed to create or initialize database automatically: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to initialize database and automatic creation failed", e);
                }
            }

            // If we reach here, rethrow original
            throw re;
        }
    }

    private static void setupDataSource(String jdbcUrl) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);

            // HikariCP configuration
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            // PostgreSQL optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            // Create tables if they don't exist (this will validate the connection)
            createTables();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempt to create the target database by connecting to the maintenance 'postgres' database.
     */
    private static void createDatabaseUsingMaintenanceDb() throws SQLException {
        // Extract database name from DB_URL
        String dbName = DB_URL.replaceFirst("^jdbc:postgresql://[^/]+/", "");
        int qIdx = dbName.indexOf('?');
        if (qIdx != -1) {
            dbName = dbName.substring(0, qIdx);
        }

        if (dbName == null || dbName.isEmpty()) {
            throw new SQLException("Unable to determine database name from DB_URL: " + DB_URL);
        }

        // Build maintenance URL (replace DB name with 'postgres')
        String maintenanceUrl = DB_URL.replaceFirst("/[^/?]+(\\?.*)?$,", "/postgres$1");
        // Fallback simpler replace when regex fails
        if (maintenanceUrl.equals(DB_URL)) {
            int lastSlash = DB_URL.lastIndexOf('/');
            if (lastSlash > -1) {
                maintenanceUrl = DB_URL.substring(0, lastSlash + 1) + "postgres";
            } else {
                throw new SQLException("Unable to construct maintenance DB URL from: " + DB_URL);
            }
        }

        logger.info("Connecting to maintenance DB to create database: {}", maintenanceUrl);

        try (Connection conn = DriverManager.getConnection(maintenanceUrl, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE DATABASE \"" + dbName + "\"";
            logger.info("Executing create database statement: {}", sql);
            stmt.executeUpdate(sql);
            logger.info("Database '{}' created successfully", dbName);
        }
    }

    /**
     * Get a connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource not initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database connection pool...");
            dataSource.close();
        }
    }

    /**
     * Create database tables if they don't exist
     */
    private static void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "\n    id VARCHAR(50) PRIMARY KEY," +
                "\n    name VARCHAR(255) NOT NULL," +
                "\n    email VARCHAR(255) NOT NULL UNIQUE," +
                "\n    role VARCHAR(50) NOT NULL," +
                "\n    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "\n)";

        String createItemsTable = "CREATE TABLE IF NOT EXISTS items (" +
                "\n    id VARCHAR(50) PRIMARY KEY," +
                "\n    name VARCHAR(255) NOT NULL," +
                "\n    price DECIMAL(10, 2) NOT NULL," +
                "\n    description TEXT," +
                "\n    status VARCHAR(50) DEFAULT 'available'," +
                "\n    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "\n)";

        String createOffersTable = "CREATE TABLE IF NOT EXISTS offers (" +
                "\n    id VARCHAR(50) PRIMARY KEY," +
                "\n    name VARCHAR(255) NOT NULL," +
                "\n    email VARCHAR(255) NOT NULL," +
                "\n    item_id VARCHAR(50) NOT NULL," +
                "\n    amount DECIMAL(10, 2) NOT NULL," +
                "\n    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "\n    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE" +
                "\n)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            logger.info("Creating database tables if they don't exist...");
            
            stmt.execute(createUsersTable);
            logger.info("Users table ready");
            
            stmt.execute(createItemsTable);
            logger.info("Items table ready");
            
            stmt.execute(createOffersTable);
            logger.info("Offers table ready");
            
        } catch (SQLException e) {
            logger.error("Error creating tables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create database tables", e);
        }
    }

    /**
     * Initialize database with sample data if tables are empty
     */
    public static void initializeSampleData() {
        try (Connection conn = getConnection()) {
            logger.info("Initializing database with sample data where needed...");

            // Check and insert users if empty
            int usersCount = 0;
            try (Statement stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) usersCount = rs.getInt(1);
            }
            if (usersCount == 0) {
                String insertUsers = "INSERT INTO users (id, name, email, role, created_at) VALUES" +
                        "\n('1', 'Rafael García', 'rafael@collectibles.com', 'admin', CURRENT_TIMESTAMP)," +
                        "\n('2', 'Ramón Torres', 'ramon@collectibles.com', 'seller', CURRENT_TIMESTAMP)," +
                        "\n('3', 'Sofía Mendez', 'sofia@collectibles.com', 'buyer', CURRENT_TIMESTAMP)" +
                        "\nON CONFLICT (id) DO NOTHING";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(insertUsers);
                    logger.info("Sample users inserted");
                }
            } else {
                logger.info("Users table already has data ({} rows), skipping user sample insert", usersCount);
            }

            // Check and insert items only if table is empty
            int itemsCount = 0;
            try (Statement stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT COUNT(*) FROM items")) {
                if (rs.next()) itemsCount = rs.getInt(1);
            }
            if (itemsCount == 0) {
                String insertItems = "INSERT INTO items (id, name, price, description, status) VALUES" +
                        "\n('1', 'Vintage Camera', 450.00, 'Classic 1960s camera in excellent condition', 'available')," +
                        "\n('2', 'Rare Comic Book', 850.00, 'First edition superhero comic', 'available')," +
                        "\n('3', 'Antique Watch', 1200.00, 'Swiss made pocket watch from 1920', 'available')," +
                        "\n('4', 'Signed Baseball', 300.00, 'Baseball signed by famous player', 'available')," +
                        "\n('5', 'Vintage Vinyl Record', 150.00, 'Limited edition album', 'available')," +
                        "\n('6', 'Classic Car Model', 500.00, 'Die-cast model car 1:18 scale', 'available')," +
                        "\n('7', 'Ancient Coin', 2000.00, 'Roman coin from 100 BC', 'available')" +
                        "\nON CONFLICT (id) DO NOTHING";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(insertItems);
                    logger.info("Sample items inserted (legacy set)");
                }
            } else {
                logger.info("Items table already has data ({} rows), skipping item sample insert", itemsCount);
            }

            // Check and insert offers only if table is empty
            int offersCount = 0;
            try (Statement stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT COUNT(*) FROM offers")) {
                if (rs.next()) offersCount = rs.getInt(1);
            }
            if (offersCount == 0) {
                String insertOffers = "INSERT INTO offers (id, name, email, item_id, amount, created_at) VALUES" +
                        "\n('offer1', 'Mario Trevino', 'mario@aroundthelab.edu', '2', 850.00, CURRENT_TIMESTAMP)," +
                        "\n('offer2', 'Lisa Heiss', 'lisa@aroundthelab.com', '3', 600.00, CURRENT_TIMESTAMP)," +
                        "\n('offer3', 'Frida Sernas', 'frida@aroundthelab.com', '5', 400.00, CURRENT_TIMESTAMP)" +
                        "\nON CONFLICT (id) DO NOTHING";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(insertOffers);
                    logger.info("Sample offers inserted");
                }
            } else {
                logger.info("Offers table already has data ({} rows), skipping offers sample insert", offersCount);
            }

        } catch (SQLException e) {
            logger.error("Error initializing sample data: {}", e.getMessage(), e);
        }
    }

    /**
     * Replace the contents of the items table with the items listed in src/main/resources/items.json
     * If the resource is not found the method will log a warning and do nothing.
     */
    public static void seedItemsFromJson() {
        InputStream is = DatabaseConfig.class.getResourceAsStream("/items.json");
        if (is == null) {
            logger.warn("Resource /items.json not found on classpath; skipping items seeding");
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Delete all existing items (offers referencing them will be cascade-deleted)
            try (Statement stmt = conn.createStatement()) {
                logger.info("Clearing existing items table before seeding from JSON...");
                stmt.executeUpdate("DELETE FROM items");
            }

            String insertSql = "INSERT INTO items (id, name, price, description, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql);
                 Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    String id = obj.has("id") ? obj.get("id").getAsString() : null;
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    String description = obj.has("description") ? obj.get("description").getAsString() : null;
                    String priceStr = obj.has("price") ? obj.get("price").getAsString() : "0";
                    BigDecimal price = parsePrice(priceStr);

                    if (id == null || name == null) {
                        logger.warn("Skipping malformed item entry in items.json: {}", obj);
                        continue;
                    }

                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.setBigDecimal(3, price);
                    ps.setString(4, description);
                    ps.setString(5, "available");
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            logger.info("Seeded items table from items.json successfully");
        } catch (Exception e) {
            logger.error("Error seeding items from JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to seed items from JSON", e);
        }
    }

    private static BigDecimal parsePrice(String raw) {
        if (raw == null) return BigDecimal.ZERO;
        // Remove currency symbols and non-digit except dot and minus
        String cleaned = raw.replaceAll("[^0-9.-]", "");
        if (cleaned.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(cleaned).setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException nfe) {
            logger.warn("Unable to parse price '{}' - falling back to 0", raw);
            return BigDecimal.ZERO;
        }
    }
}
