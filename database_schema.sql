-- =====================================================
-- MAKAN FOR YOU - DATABASE SCHEMA
-- =====================================================
-- This SQL script creates tables for a home-based cooking
-- delivery application with user management, kitchen inventory,
-- orders, payments, and delivery tracking.

-- =====================================================
-- 1. USER TABLE - User Management and Authentication
-- =====================================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone_number),
    INDEX idx_email (email),
    INDEX idx_registration_date (registration_date)
);

-- =====================================================
-- 2. KITCHEN TABLE - Home-Based Kitchen Information
-- =====================================================
CREATE TABLE kitchens (
    kitchen_id INT PRIMARY KEY AUTO_INCREMENT,
    kitchen_name VARCHAR(100) NOT NULL,
    kitchen_address VARCHAR(255) NOT NULL,
    kitchen_owner_name VARCHAR(100) NOT NULL,
    owner_user_id INT NOT NULL,
    kitchen_owner_contact VARCHAR(15) NOT NULL,
    kitchen_owner_email VARCHAR(100) NOT NULL,
    kitchen_alternate_contact VARCHAR(15),
    kitchen_alternate_email VARCHAR(100),
    kitchen_description TEXT,
    kitchen_city VARCHAR(50),
    kitchen_state VARCHAR(50),
    kitchen_postal_code VARCHAR(10),
    kitchen_country VARCHAR(50),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    rating DECIMAL(3, 2) DEFAULT 0,
    total_orders INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    verified BOOLEAN DEFAULT FALSE,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kitchen_name (kitchen_name),
    INDEX idx_city (kitchen_city),
    INDEX idx_is_active (is_active),
    INDEX idx_owner_user_id (owner_user_id),
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FULLTEXT INDEX ft_search (kitchen_name, kitchen_address)
);

-- =====================================================
-- 3. KITCHEN MENU TABLE - Menu Items and Details
-- =====================================================
CREATE TABLE kitchen_menu (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    kitchen_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    item_description TEXT,
    item_ingredients VARCHAR(500),
    item_allergic_indication VARCHAR(200),
    item_cost DECIMAL(10, 2) NOT NULL,
    item_image_path VARCHAR(255),
    item_available_timing VARCHAR(100),
    item_is_active BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INT DEFAULT 30,
    item_quantity_available INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (kitchen_id) REFERENCES kitchens(kitchen_id) ON DELETE CASCADE,
    INDEX idx_kitchen_id (kitchen_id),
    INDEX idx_item_name (item_name),
    INDEX idx_is_active (item_is_active),
    FULLTEXT INDEX ft_item_search (item_name, item_description)
);

-- =====================================================
-- 4. ORDER TABLE - Customer Orders
-- =====================================================
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    kitchen_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_total DECIMAL(10, 2) NOT NULL,
    order_status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    confirmation_by_kitchen BOOLEAN DEFAULT FALSE,
    confirmation_timestamp TIMESTAMP NULL,
    delivery_address VARCHAR(255) NOT NULL,
    delivery_city VARCHAR(50),
    delivery_state VARCHAR(50),
    delivery_postal_code VARCHAR(10),
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (kitchen_id) REFERENCES kitchens(kitchen_id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_kitchen_id (kitchen_id),
    INDEX idx_order_date (order_date),
    INDEX idx_order_status (order_status)
);

-- =====================================================
-- 5. ORDER ITEMS TABLE - Individual Items in Orders
-- =====================================================
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    item_quantity INT NOT NULL,
    item_unit_price DECIMAL(10, 2) NOT NULL,
    item_total DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES kitchen_menu(item_id) ON DELETE RESTRICT,
    INDEX idx_order_id (order_id),
    INDEX idx_item_id (item_id)
);

-- =====================================================
-- 6. PAYMENT TABLE - Order Payments
-- =====================================================
CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    user_id INT NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING', 'WALLET', 'CASH_ON_DELIVERY', 'UPI') DEFAULT 'CASH_ON_DELIVERY',
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP,
    refund_amount DECIMAL(10, 2) DEFAULT 0,
    refund_date TIMESTAMP NULL,
    refund_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_date (payment_date)
);

-- =====================================================
-- 7. DELIVERY TABLE - Order Delivery Tracking
-- =====================================================
CREATE TABLE deliveries (
    delivery_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    kitchen_id INT NOT NULL,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    delivery_status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED') DEFAULT 'PENDING',
    assigned_to VARCHAR(100),
    pickup_time TIMESTAMP NULL,
    delivery_time TIMESTAMP NULL,
    estimated_delivery_time TIMESTAMP,
    current_location VARCHAR(255),
    delivery_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (kitchen_id) REFERENCES kitchens(kitchen_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (item_id) REFERENCES kitchen_menu(item_id) ON DELETE RESTRICT,
    INDEX idx_order_id (order_id),
    INDEX idx_kitchen_id (kitchen_id),
    INDEX idx_user_id (user_id),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_delivery_time (delivery_time)
);

-- =====================================================
-- 8. REVIEWS TABLE - Kitchen and Food Reviews
-- =====================================================
CREATE TABLE reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    kitchen_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    helpful_count INT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (kitchen_id) REFERENCES kitchens(kitchen_id) ON DELETE CASCADE,
    INDEX idx_kitchen_id (kitchen_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating)
);

-- =====================================================
-- 9. SEARCH LOG TABLE - Track Search Queries
-- =====================================================
CREATE TABLE search_logs (
    search_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    search_query VARCHAR(255) NOT NULL,
    search_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    results_count INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_search_query (search_query),
    INDEX idx_search_date (search_date)
);

-- =====================================================
-- INDEXES FOR BETTER SEARCH PERFORMANCE
-- =====================================================

-- Composite index for searching kitchens by location and name
ALTER TABLE kitchens ADD INDEX idx_city_active (kitchen_city, is_active);

-- Composite index for filtering menu items by kitchen and availability
ALTER TABLE kitchen_menu ADD INDEX idx_kitchen_active (kitchen_id, item_is_active);

-- Composite index for order history queries
ALTER TABLE orders ADD INDEX idx_user_order_date (user_id, order_date);

-- Composite index for kitchen order history
ALTER TABLE orders ADD INDEX idx_kitchen_order_date (kitchen_id, order_date);

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for active kitchens with their menu counts
CREATE VIEW kitchen_summary AS
SELECT
    k.kitchen_id,
    k.kitchen_name,
    k.kitchen_address,
    k.kitchen_city,
    k.rating,
    k.total_orders,
    COUNT(km.item_id) AS active_menu_items
FROM kitchens k
LEFT JOIN kitchen_menu km ON k.kitchen_id = km.kitchen_id AND km.item_is_active = TRUE
WHERE k.is_active = TRUE
GROUP BY k.kitchen_id, k.kitchen_name, k.kitchen_address, k.kitchen_city, k.rating, k.total_orders;

-- View for order details with all related information
CREATE VIEW order_details_view AS
SELECT
    o.order_id,
    o.order_date,
    u.user_id,
    u.first_name,
    u.last_name,
    u.email,
    u.phone_number,
    k.kitchen_id,
    k.kitchen_name,
    k.kitchen_owner_name,
    o.order_total,
    o.order_status,
    o.confirmation_by_kitchen,
    p.payment_status,
    d.delivery_status
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN kitchens k ON o.kitchen_id = k.kitchen_id
LEFT JOIN payments p ON o.order_id = p.order_id
LEFT JOIN deliveries d ON o.order_id = d.order_id;

-- =====================================================
-- END OF SCHEMA
-- =====================================================
