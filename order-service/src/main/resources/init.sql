-- Create database
CREATE DATABASE IF NOT EXISTS order_db;

-- Use database
USE order_db;

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_number (order_number),
    INDEX idx_status (status)
);

-- Create order_line_items table
CREATE TABLE IF NOT EXISTS order_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_code VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_sku_code (sku_code)
);

-- Create indexes for performance
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_price ON order_line_items(price);
