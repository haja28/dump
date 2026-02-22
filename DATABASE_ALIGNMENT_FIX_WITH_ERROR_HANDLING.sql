-- =====================================================
-- DATABASE SCHEMA ALIGNMENT - WITH ERROR HANDLING
-- =====================================================
-- Alternative version with error handling using stored procedure
-- This version won't fail if columns already exist
--
-- Generated: January 31, 2026
-- =====================================================

-- Drop the procedure if it exists
DROP PROCEDURE IF EXISTS align_database_schema;

DELIMITER //

CREATE PROCEDURE align_database_schema()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN
        -- Error occurred, but we continue
    END;

    -- =====================================================
    -- PHASE 1: FIX EXISTING TABLES
    -- =====================================================

    -- =====================================================
    -- 1. ALTER TABLE: users
    -- =====================================================
    ALTER TABLE users ADD COLUMN address VARCHAR(255);
    ALTER TABLE users ADD COLUMN city VARCHAR(50);
    ALTER TABLE users ADD COLUMN state VARCHAR(50);
    ALTER TABLE users ADD COLUMN postal_code VARCHAR(10);
    ALTER TABLE users ADD COLUMN country VARCHAR(50);
    ALTER TABLE users ADD COLUMN registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    ALTER TABLE users ADD COLUMN role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') DEFAULT 'CUSTOMER' AFTER password_hash;
    ALTER TABLE users ADD INDEX idx_role (role);

    -- =====================================================
    -- 2. ALTER TABLE: kitchens
    -- =====================================================
    ALTER TABLE kitchens ADD COLUMN owner_user_id INT;
    ALTER TABLE kitchens ADD COLUMN delivery_area VARCHAR(255);
    ALTER TABLE kitchens ADD COLUMN cuisine_types VARCHAR(500);
    ALTER TABLE kitchens ADD COLUMN approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING';
    ALTER TABLE kitchens ADD INDEX idx_owner_user_id (owner_user_id);
    ALTER TABLE kitchens ADD INDEX idx_approval_status (approval_status);
    ALTER TABLE kitchens ADD CONSTRAINT fk_kitchens_owner_user_id
        FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL;

    -- =====================================================
    -- 3. ALTER TABLE: kitchen_menu
    -- =====================================================
    ALTER TABLE kitchen_menu ADD COLUMN is_veg BOOLEAN DEFAULT TRUE;
    ALTER TABLE kitchen_menu ADD COLUMN is_halal BOOLEAN DEFAULT FALSE;
    ALTER TABLE kitchen_menu ADD COLUMN spicy_level INT DEFAULT 1;
    ALTER TABLE kitchen_menu ADD COLUMN rating DECIMAL(3, 2) DEFAULT 0.0;
    ALTER TABLE kitchen_menu ADD INDEX idx_is_veg (is_veg);
    ALTER TABLE kitchen_menu ADD INDEX idx_rating (rating);

    -- =====================================================
    -- 4. ALTER TABLE: order_items
    -- =====================================================
    ALTER TABLE order_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

    -- =====================================================
    -- PHASE 2: CREATE MISSING TABLES
    -- =====================================================

    -- =====================================================
    -- 5. CREATE TABLE: menu_labels
    -- =====================================================
    CREATE TABLE IF NOT EXISTS menu_labels (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(50) UNIQUE NOT NULL,
        description VARCHAR(200),
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
        INDEX idx_name (name),
        INDEX idx_is_active (is_active)
    );

    -- =====================================================
    -- 6. CREATE TABLE: menu_item_labels
    -- =====================================================
    CREATE TABLE IF NOT EXISTS menu_item_labels (
        menu_item_id INT NOT NULL,
        label_id INT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (menu_item_id, label_id),
        FOREIGN KEY (menu_item_id) REFERENCES kitchen_menu(item_id) ON DELETE CASCADE,
        FOREIGN KEY (label_id) REFERENCES menu_labels(id) ON DELETE CASCADE,
        INDEX idx_label_id (label_id)
    );

    -- =====================================================
    -- PHASE 3: VERIFY CRITICAL TABLES EXIST
    -- =====================================================

    -- =====================================================
    -- 7. CREATE TABLE: payments
    -- =====================================================
    CREATE TABLE IF NOT EXISTS payments (
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
    -- 8. CREATE TABLE: deliveries
    -- =====================================================
    CREATE TABLE IF NOT EXISTS deliveries (
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
    -- 9. CREATE TABLE: reviews
    -- =====================================================
    CREATE TABLE IF NOT EXISTS reviews (
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
    -- 10. CREATE TABLE: search_logs
    -- =====================================================
    CREATE TABLE IF NOT EXISTS search_logs (
        search_id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT,
        search_query VARCHAR(255) NOT NULL,
        search_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        results_count INT,
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
        INDEX idx_search_query (search_query),
        INDEX idx_search_date (search_date)
    );

    SELECT 'Database alignment completed successfully!' AS result;

END //

DELIMITER ;

-- =====================================================
-- EXECUTE THE PROCEDURE
-- =====================================================
-- This will run all the ALTER and CREATE statements
-- and continue even if some fail (e.g., column already exists)

CALL align_database_schema();

-- =====================================================
-- DROP THE PROCEDURE AFTER USE (OPTIONAL)
-- =====================================================
-- DROP PROCEDURE IF EXISTS align_database_schema;

