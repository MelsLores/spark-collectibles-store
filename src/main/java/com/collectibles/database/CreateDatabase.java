package com.collectibles.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to create the target PostgreSQL database by connecting to the maintenance DB.
 *
 * Usage (PowerShell):
 *  mvn -Dexec.mainClass="com.collectibles.database.CreateDatabase" exec:java
 *
 * Optional environment variables:
 *  DB_HOST (default: localhost)
 *  DB_PORT (default: 5432)
 *  DB_NAME (default: collectibles_db)
 *  DB_USER (default: postgres)
 *  DB_PASSWORD (default: postgres)
 */
public class CreateDatabase {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String dbName = System.getenv().getOrDefault("DB_NAME", "collectibles_db");
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "postgres");

        String maintenanceUrl = String.format("jdbc:postgresql://%s:%s/postgres", host, port);

        System.out.printf("Creating database '%s' on %s as user '%s'...%n", dbName, host + ":" + port, user);

        try (Connection conn = DriverManager.getConnection(maintenanceUrl, user, password);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE DATABASE \"" + dbName + "\"";
            stmt.executeUpdate(sql);

            System.out.println("Database created successfully: " + dbName);
        } catch (SQLException e) {
            System.err.println("Failed to create database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
