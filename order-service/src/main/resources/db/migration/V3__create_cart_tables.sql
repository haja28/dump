-- =====================================================
-- Cart Management Database Schema
-- =====================================================

-- Create carts table
CREATE TABLE IF NOT EXISTS carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    kitchen_id BIGINT,
    kitchen_name VARCHAR(100),
    coupon_code VARCHAR(50),
    coupon_description VARCHAR(255),
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    delivery_fee DECIMAL(10, 2) DEFAULT 3.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_cart_user_id (user_id),
    INDEX idx_cart_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cart_items table
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(100),
    item_description VARCHAR(500),
    image_url VARCHAR(500),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    current_menu_price DECIMAL(10, 2),
    price_changed BOOLEAN DEFAULT FALSE,
    in_stock BOOLEAN DEFAULT TRUE,
    available_stock INT,
    special_requests VARCHAR(500),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_cart_item_cart_id (cart_id),
    INDEX idx_cart_item_item_id (item_id),

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data for Testing
-- =====================================================

-- Insert sample cart
INSERT INTO carts (user_id, kitchen_id, kitchen_name, delivery_fee) VALUES
(1, 1, 'Priya''s Kitchen', 3.00),
(2, 2, 'Ahmad''s Homemade', 3.50);

-- Insert sample cart items with stock and price tracking
INSERT INTO cart_items (cart_id, item_id, item_name, item_description, image_url, quantity, unit_price, original_price, in_stock, available_stock, special_requests) VALUES
(1, 1, 'Chicken Biryani', 'Authentic Hyderabadi biryani with tender chicken', 'https://example.com/biryani.jpg', 2, 12.99, 12.99, TRUE, 50, 'Extra spicy'),
(1, 2, 'Paneer Tikka Masala', 'Creamy tomato-based curry with paneer', 'https://example.com/paneer.jpg', 1, 10.50, 10.50, TRUE, 30, NULL),
(2, 4, 'Nasi Lemak Special', 'Coconut rice with sambal and fried chicken', 'https://example.com/nasilemak.jpg', 3, 8.99, 8.99, TRUE, 25, 'Extra sambal');
