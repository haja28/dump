-- =====================================================
-- Coupon Service Database Schema
-- =====================================================

-- Create coupons table
CREATE TABLE IF NOT EXISTS coupons (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2),
    min_order_amount DECIMAL(10, 2),
    max_uses INT,
    current_uses INT DEFAULT 0,
    max_uses_per_user INT DEFAULT 1,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    applicable_to VARCHAR(20) DEFAULT 'ALL',
    kitchen_id BIGINT,
    is_first_order_only BOOLEAN DEFAULT FALSE,
    is_new_user_only BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_coupon_code (code),
    INDEX idx_coupon_status (status),
    INDEX idx_coupon_valid_dates (valid_from, valid_until),
    INDEX idx_coupon_kitchen (kitchen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create coupon_redemptions table
CREATE TABLE IF NOT EXISTS coupon_redemptions (
    redemption_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    order_amount DECIMAL(10, 2),
    discount_applied DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    INDEX idx_redemption_coupon (coupon_id),
    INDEX idx_redemption_user (user_id),
    INDEX idx_redemption_order (order_id),
    INDEX idx_redemption_user_coupon (user_id, coupon_id),

    CONSTRAINT fk_redemption_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Coupons for Testing
-- =====================================================

-- Percentage discount coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_uses, max_uses_per_user, valid_until, status, applicable_to) VALUES
('SAVE10', '10% Off', 'Get 10% off your order', 'PERCENTAGE', 10.00, 15.00, 20.00, 1000, 3, DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'ALL'),
('SAVE20', '20% Off', 'Get 20% off your order', 'PERCENTAGE', 20.00, 25.00, 30.00, 500, 2, DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'ALL'),
('VIP50', 'VIP 50% Off', 'Exclusive 50% discount for VIP members', 'PERCENTAGE', 50.00, 50.00, 50.00, 100, 1, DATE_ADD(NOW(), INTERVAL 90 DAY), 'ACTIVE', 'ALL');

-- Fixed amount discount coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, min_order_amount, max_uses, max_uses_per_user, valid_until, status, applicable_to) VALUES
('SAVE5', 'RM5 Off', 'Get RM5 off your order', 'FIXED_AMOUNT', 5.00, 25.00, 2000, 5, DATE_ADD(NOW(), INTERVAL 60 DAY), 'ACTIVE', 'ALL'),
('FLAT10', 'RM10 Off', 'Get RM10 off your order', 'FIXED_AMOUNT', 10.00, 40.00, 1000, 3, DATE_ADD(NOW(), INTERVAL 60 DAY), 'ACTIVE', 'ALL'),
('MEGA20', 'RM20 Off', 'Mega discount - RM20 off', 'FIXED_AMOUNT', 20.00, 60.00, 500, 1, DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'ALL');

-- Free delivery coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, min_order_amount, max_uses, max_uses_per_user, valid_until, status, applicable_to) VALUES
('FREESHIP', 'Free Delivery', 'Free delivery on your order', 'FREE_DELIVERY', 5.00, 15.00, 5000, 10, DATE_ADD(NOW(), INTERVAL 90 DAY), 'ACTIVE', 'DELIVERY_ONLY'),
('NODELIVERY', 'No Delivery Fee', 'Waive delivery fee completely', 'FREE_DELIVERY', 10.00, 30.00, 1000, 5, DATE_ADD(NOW(), INTERVAL 60 DAY), 'ACTIVE', 'DELIVERY_ONLY');

-- New user coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_uses_per_user, valid_until, status, applicable_to, is_new_user_only) VALUES
('WELCOME', 'Welcome Discount', 'Welcome to MakanForYou! Enjoy 25% off your first order', 'PERCENTAGE', 25.00, 20.00, 20.00, 1, DATE_ADD(NOW(), INTERVAL 365 DAY), 'ACTIVE', 'ALL', TRUE),
('NEWBIE', 'New User Special', 'Special discount for new users - RM15 off', 'FIXED_AMOUNT', 15.00, NULL, 35.00, 1, DATE_ADD(NOW(), INTERVAL 365 DAY), 'ACTIVE', 'ALL', TRUE);

-- First order coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, min_order_amount, max_uses_per_user, valid_until, status, applicable_to, is_first_order_only) VALUES
('FIRSTORDER', 'First Order Discount', '30% off your first order', 'PERCENTAGE', 30.00, 25.00, 1, DATE_ADD(NOW(), INTERVAL 365 DAY), 'ACTIVE', 'ALL', TRUE);

-- Limited time / flash sale coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_uses, max_uses_per_user, valid_from, valid_until, status, applicable_to) VALUES
('FLASH40', 'Flash Sale 40% Off', 'Limited time flash sale - 40% off!', 'PERCENTAGE', 40.00, 30.00, 30.00, 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE', 'ALL'),
('WEEKEND', 'Weekend Special', 'Weekend special - RM8 off', 'FIXED_AMOUNT', 8.00, NULL, 20.00, 500, 2, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 'ACTIVE', 'ALL');

-- Inactive/Expired coupons for testing
INSERT INTO coupons (code, name, description, discount_type, discount_value, valid_until, status, applicable_to) VALUES
('EXPIRED50', 'Expired Coupon', 'This coupon has expired', 'PERCENTAGE', 50.00, DATE_SUB(NOW(), INTERVAL 7 DAY), 'EXPIRED', 'ALL'),
('INACTIVE', 'Inactive Coupon', 'This coupon is inactive', 'FIXED_AMOUNT', 100.00, DATE_ADD(NOW(), INTERVAL 30 DAY), 'INACTIVE', 'ALL');
