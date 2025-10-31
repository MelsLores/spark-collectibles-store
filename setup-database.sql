-- Script de inicialización de base de datos PostgreSQL
-- Para Collectibles Store Application

-- Crear la base de datos
CREATE DATABASE collectibles_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Conectar a la base de datos
\c collectibles_db

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('admin', 'seller', 'buyer')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de items (artículos coleccionables)
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    description TEXT,
    status VARCHAR(50) DEFAULT 'available' CHECK (status IN ('available', 'sold', 'pending')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de ofertas
CREATE TABLE IF NOT EXISTS offers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    item_id VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_price ON items(price);
CREATE INDEX idx_offers_item_id ON offers(item_id);
CREATE INDEX idx_offers_email ON offers(email);

-- Insertar datos de ejemplo - Usuarios
INSERT INTO users (id, name, email, role, created_at) VALUES
('1', 'Rafael García', 'rafael@collectibles.com', 'admin', CURRENT_TIMESTAMP),
('2', 'Ramón Torres', 'ramon@collectibles.com', 'seller', CURRENT_TIMESTAMP),
('3', 'Sofía Mendez', 'sofia@collectibles.com', 'buyer', CURRENT_TIMESTAMP),
('4', 'María González', 'maria@collectibles.com', 'buyer', CURRENT_TIMESTAMP),
('5', 'Carlos Ruiz', 'carlos@collectibles.com', 'seller', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Insertar datos de ejemplo - Items
INSERT INTO items (id, name, price, description, status, created_at) VALUES
('1', 'Vintage Camera', 450.00, 'Classic 1960s camera in excellent condition. Perfect for collectors and photography enthusiasts.', 'available', CURRENT_TIMESTAMP),
('2', 'Rare Comic Book', 850.00, 'First edition superhero comic from the golden age. Excellent condition with original cover.', 'available', CURRENT_TIMESTAMP),
('3', 'Antique Watch', 1200.00, 'Swiss made pocket watch from 1920. Fully functional with original mechanism.', 'available', CURRENT_TIMESTAMP),
('4', 'Signed Baseball', 300.00, 'Baseball signed by famous player from 1990s. Includes certificate of authenticity.', 'available', CURRENT_TIMESTAMP),
('5', 'Vintage Vinyl Record', 150.00, 'Limited edition album from legendary rock band. Mint condition, never played.', 'available', CURRENT_TIMESTAMP),
('6', 'Classic Car Model', 500.00, 'Die-cast model car 1:18 scale. Limited edition collector item.', 'available', CURRENT_TIMESTAMP),
('7', 'Ancient Coin', 2000.00, 'Roman coin from 100 BC. Authenticated by expert numismatists.', 'available', CURRENT_TIMESTAMP),
('8', 'Vintage Poster', 180.00, 'Movie poster from 1950s cinema. Original print in excellent condition.', 'available', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Insertar datos de ejemplo - Ofertas
INSERT INTO offers (id, name, email, item_id, amount, created_at) VALUES
('offer1', 'Mario Trevino', 'mario@aroundthelab.edu', '2', 850.00, CURRENT_TIMESTAMP),
('offer2', 'Lisa Heiss', 'lisa@aroundthelab.com', '3', 600.00, CURRENT_TIMESTAMP),
('offer3', 'Frida Sernas', 'frida@aroundthelab.com', '5', 400.00, CURRENT_TIMESTAMP),
('offer4', 'Juan Pérez', 'juan@example.com', '1', 420.00, CURRENT_TIMESTAMP),
('offer5', 'Ana López', 'ana@example.com', '7', 1800.00, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Verificar que los datos se insertaron correctamente
SELECT 'Users:' as table_name, COUNT(*) as total_records FROM users
UNION ALL
SELECT 'Items:', COUNT(*) FROM items
UNION ALL
SELECT 'Offers:', COUNT(*) FROM offers;

-- Mostrar todos los datos
SELECT '=== USERS ===' as section;
SELECT * FROM users ORDER BY created_at;

SELECT '=== ITEMS ===' as section;
SELECT * FROM items ORDER BY price DESC;

SELECT '=== OFFERS ===' as section;
SELECT o.id, o.name, o.email, i.name as item_name, o.amount, o.created_at 
FROM offers o 
JOIN items i ON o.item_id = i.id 
ORDER BY o.created_at DESC;
