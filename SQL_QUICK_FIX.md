# Quick Reference: All ALTER Statements

## Copy-Paste Ready SQL Statements

### Run in sequence on your database:

```sql
-- =====================================================
-- ALIGNMENT UPDATE 1: Fix users table
-- =====================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS (
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') DEFAULT 'CUSTOMER' AFTER password_hash;

ALTER TABLE users ADD INDEX IF NOT EXISTS idx_role (role);


-- =====================================================
-- ALIGNMENT UPDATE 2: Fix kitchens table
-- =====================================================
ALTER TABLE kitchens ADD COLUMN IF NOT EXISTS (
    owner_user_id INT,
    delivery_area VARCHAR(255),
    cuisine_types VARCHAR(500),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'
);

ALTER TABLE kitchens ADD INDEX IF NOT EXISTS idx_owner_user_id (owner_user_id);
ALTER TABLE kitchens ADD INDEX IF NOT EXISTS idx_approval_status (approval_status);

ALTER TABLE kitchens ADD CONSTRAINT IF NOT EXISTS fk_kitchens_owner_user_id 
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL;


-- =====================================================
-- ALIGNMENT UPDATE 3: Fix kitchen_menu table
-- =====================================================
ALTER TABLE kitchen_menu ADD COLUMN IF NOT EXISTS (
    is_veg BOOLEAN DEFAULT TRUE,
    is_halal BOOLEAN DEFAULT FALSE,
    spicy_level INT DEFAULT 1,
    rating DECIMAL(3, 2) DEFAULT 0.0
);

ALTER TABLE kitchen_menu ADD INDEX IF NOT EXISTS idx_is_veg (is_veg);
ALTER TABLE kitchen_menu ADD INDEX IF NOT EXISTS idx_rating (rating);


-- =====================================================
-- ALIGNMENT UPDATE 4: Fix order_items table
-- =====================================================
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;


-- =====================================================
-- ALIGNMENT UPDATE 5: Create menu_labels table
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
-- ALIGNMENT UPDATE 6: Create menu_item_labels junction table
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
-- Verify payments, deliveries, reviews, search_logs exist
-- =====================================================
-- These should already exist from your schema.sql
-- If needed, they're in DATABASE_ALIGNMENT_FIX.sql
```

---

## Entity Update Required

### File: order-service/src/main/java/com/makanforyou/order/entity/OrderItem.java

Add the missing field:

```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
```

Full updated class:

```java
package com.makanforyou.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer itemQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal itemUnitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;

    @Column(length = 500)
    private String specialRequests;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## Summary of Changes

| Component | Type | Change |
|-----------|------|--------|
| users | ALTER | Add 7 new columns (address, city, state, postal_code, country, registration_date, role) |
| kitchens | ALTER | Add 4 new columns (owner_user_id, delivery_area, cuisine_types, approval_status) |
| kitchen_menu | ALTER | Add 4 new columns (is_veg, is_halal, spicy_level, rating) |
| order_items | ALTER | Add 1 new column (created_at) |
| menu_labels | CREATE | New table for menu item labels |
| menu_item_labels | CREATE | New junction table for MenuItem-MenuLabel relationship |
| OrderItem | UPDATE | Add createdAt field to entity |

---

## Required Additional Entities (Not in Application)

These entities exist in SQL but are missing from the application code:

1. **Payment** - payment-service/src/main/java/com/makanforyou/payment/entity/Payment.java
2. **Delivery** - order-service/src/main/java/com/makanforyou/order/entity/Delivery.java
3. **Review** - Create new review-service or add to menu-service

---

## Validation Steps

After applying all changes:

```sql
-- Verify all new columns exist
DESCRIBE users;
DESCRIBE kitchens;
DESCRIBE kitchen_menu;
DESCRIBE order_items;

-- Verify new tables exist
SHOW TABLES LIKE 'menu_%';

-- Verify indexes
SHOW INDEX FROM users;
SHOW INDEX FROM kitchens;

-- Verify foreign keys
SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE REFERENCED_TABLE_NAME IS NOT NULL AND TABLE_SCHEMA = 'your_db_name';
```

