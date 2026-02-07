# Database Schema vs Application Code Alignment Analysis

## Overview
Analysis of application code entities against the SQL database schema to identify misalignments and required ALTER statements.

---

## FINDINGS SUMMARY

### ✅ ALIGNED TABLES
1. **users** - Aligned (User entity has most required fields)
2. **kitchens** - Partially aligned (missing some SQL fields in entity)
3. **kitchen_menu** - Partially aligned (extra fields in entity, missing some SQL fields)
4. **orders** - Aligned
5. **order_items** - Missing createdAt field
6. **menu_labels** - Entity exists but no SQL table in schema

### ❌ MISSING TABLES (No Java Entities)
1. **payments** - Not implemented
2. **deliveries** - Not implemented
3. **reviews** - Not implemented
4. **search_logs** - Not implemented

### ❌ MISSING JUNCTION TABLE (No SQL table)
1. **menu_item_labels** - Referenced in MenuItem entity but no SQL table

---

## DETAILED ANALYSIS

### 1. TABLE: users
**SQL Schema Fields:**
- user_id, first_name, last_name, email, phone_number, password_hash, address, city, state, postal_code, country, registration_date, last_login, is_active, created_at, updated_at

**Java Entity (User):**
- id, firstName, lastName, email, phoneNumber, passwordHash, role, isActive, createdAt, updatedAt, lastLogin

**Alignment Status:** ⚠️ PARTIAL
- **Missing in SQL Schema:** role (ENUM field)
- **Missing in Entity:** address, city, state, postal_code, country, registration_date
- **Action:** Add missing fields to database

---

### 2. TABLE: kitchens
**SQL Schema Fields:**
- kitchen_id, kitchen_name, kitchen_address, kitchen_owner_name, kitchen_owner_contact, kitchen_owner_email, kitchen_alternate_contact, kitchen_alternate_email, kitchen_description, kitchen_city, kitchen_state, kitchen_postal_code, kitchen_country, latitude, longitude, rating, total_orders, is_active, verified, registration_date, created_at, updated_at

**Java Entity (Kitchen):**
- id, kitchenName, ownerUserId, description, address, city, state, postalCode, country, latitude, longitude, deliveryArea, cuisineTypes, ownerContact, ownerEmail, alternateContact, alternateEmail, approvalStatus, rating, totalOrders, isActive, verified, createdAt, updatedAt

**Alignment Status:** ⚠️ PARTIAL
- **Missing in Entity:** kitchen_owner_name
- **Extra in Entity:** ownerUserId (relationship), deliveryArea, cuisineTypes, approvalStatus
- **Missing in SQL:** ownerUserId, deliveryArea, cuisineTypes, approvalStatus
- **Discrepancy:** SQL has registration_date, Entity uses createdAt

---

### 3. TABLE: kitchen_menu
**SQL Schema Fields:**
- item_id, kitchen_id, item_name, item_description, item_ingredients, item_allergic_indication, item_cost, item_image_path, item_available_timing, item_is_active, preparation_time_minutes, item_quantity_available, created_at, updated_at

**Java Entity (MenuItem):**
- id, kitchenId, itemName, description, ingredients, allergyIndication, cost, imagePath, availableTiming, isActive, preparationTimeMinutes, quantityAvailable, isVeg, isHalal, spicyLevel, rating, labels (relationship), createdAt, updatedAt

**Alignment Status:** ⚠️ PARTIAL
- **Extra in Entity:** isVeg, isHalal, spicyLevel, rating, labels (relationship)
- **Missing in SQL:** isVeg, isHalal, spicyLevel, rating (for individual items)
- **Field Name Mapping:** item_description ↔ description

---

### 4. TABLE: orders
**SQL Schema Fields:**
- order_id, user_id, kitchen_id, order_date, order_total, order_status, confirmation_by_kitchen, confirmation_timestamp, delivery_address, delivery_city, delivery_state, delivery_postal_code, special_instructions, created_at, updated_at

**Java Entity (Order):**
- id, userId, kitchenId, orderTotal, orderStatus, confirmationByKitchen, confirmationTimestamp, deliveryAddress, deliveryCity, deliveryState, deliveryPostalCode, specialInstructions, createdAt, updatedAt

**Alignment Status:** ✅ FULLY ALIGNED
- **Discrepancy:** SQL uses order_date (DEFAULT CURRENT_TIMESTAMP), Entity uses createdAt
- **Note:** Entity missing order_date field (uses createdAt instead - acceptable pattern)

---

### 5. TABLE: order_items
**SQL Schema Fields:**
- order_item_id, order_id, item_id, item_quantity, item_unit_price, item_total, special_requests, created_at

**Java Entity (OrderItem):**
- id, orderId, itemId, itemQuantity, itemUnitPrice, itemTotal, specialRequests

**Alignment Status:** ❌ MISSING FIELD
- **Missing in Entity:** createdAt (required in SQL)
- **Action:** Add createdAt field to OrderItem entity

---

### 6. TABLE: payments
**SQL Schema Fields:**
- payment_id, order_id, user_id, payment_amount, payment_method (ENUM), payment_status (ENUM), transaction_id, payment_date, refund_amount, refund_date, refund_reason, created_at, updated_at

**Java Entity:** ❌ DOES NOT EXIST
- **Action:** Create Payment entity and corresponding service

---

### 7. TABLE: deliveries
**SQL Schema Fields:**
- delivery_id, order_id, kitchen_id, user_id, item_id, delivery_status (ENUM), assigned_to, pickup_time, delivery_time, estimated_delivery_time, current_location, delivery_notes, created_at, updated_at

**Java Entity:** ❌ DOES NOT EXIST
- **Action:** Create Delivery entity and corresponding service

---

### 8. TABLE: reviews
**SQL Schema Fields:**
- review_id, order_id, user_id, kitchen_id, rating, review_text, review_date, helpful_count

**Java Entity:** ❌ DOES NOT EXIST
- **Action:** Create Review entity and corresponding service

---

### 9. TABLE: search_logs
**SQL Schema Fields:**
- search_id, user_id, search_query, search_date, results_count

**Java Entity:** ❌ DOES NOT EXIST
- **Action:** Create SearchLog entity (optional for logging)

---

### 10. JUNCTION TABLE: menu_item_labels (Extra in Application)
**SQL Schema:** ❌ DOES NOT EXIST
**Java Entity Reference:** MenuItem has @ManyToMany relationship with MenuLabel using this table

**Alignment Status:** ❌ TABLE MISSING
- **Required SQL:** Need junction table menu_item_labels
- **Action:** Create menu_item_labels junction table in SQL

---

### 11. TABLE: menu_labels (Extra in Application)
**SQL Schema:** ❌ DOES NOT EXIST
**Java Entity:** MenuLabel exists in menu-service

**Alignment Status:** ❌ TABLE MISSING
- **Note:** This table should be added to SQL schema for label support

---

## REQUIRED ALTER STATEMENTS

### Phase 1: Fix Existing Tables

#### 1. Alter users table
```sql
-- Add missing fields to users table
ALTER TABLE users ADD COLUMN (
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add role field to support user types (CUSTOMER, KITCHEN, ADMIN)
ALTER TABLE users ADD COLUMN role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') DEFAULT 'CUSTOMER' AFTER password_hash;

-- Add index for role queries
ALTER TABLE users ADD INDEX idx_role (role);
```

#### 2. Alter kitchens table
```sql
-- Add missing fields
ALTER TABLE kitchens ADD COLUMN (
    owner_user_id INT,
    delivery_area VARCHAR(255),
    cuisine_types VARCHAR(500),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'
);

-- Add indexes
ALTER TABLE kitchens ADD INDEX idx_owner_user_id (owner_user_id);
ALTER TABLE kitchens ADD INDEX idx_approval_status (approval_status);

-- Add foreign key for owner_user_id
ALTER TABLE kitchens ADD CONSTRAINT fk_kitchens_owner_user_id 
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL;

-- Drop old owner fields if migrating data, otherwise keep for backward compatibility
-- ALTER TABLE kitchens DROP COLUMN kitchen_owner_name;
-- ALTER TABLE kitchens DROP COLUMN kitchen_owner_contact;
-- ALTER TABLE kitchens DROP COLUMN kitchen_owner_email;
```

#### 3. Alter kitchen_menu table
```sql
-- Add missing fields
ALTER TABLE kitchen_menu ADD COLUMN (
    is_veg BOOLEAN DEFAULT TRUE,
    is_halal BOOLEAN DEFAULT FALSE,
    spicy_level INT DEFAULT 1,
    rating DECIMAL(3, 2) DEFAULT 0.0
);

-- Add indexes for new fields
ALTER TABLE kitchen_menu ADD INDEX idx_is_veg (is_veg);
ALTER TABLE kitchen_menu ADD INDEX idx_rating (rating);
```

#### 4. Alter order_items table
```sql
-- Add missing created_at field
ALTER TABLE order_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
```

### Phase 2: Create Missing Tables

#### 5. Create menu_labels table (for application support)
```sql
CREATE TABLE menu_labels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
);
```

#### 6. Create menu_item_labels junction table
```sql
CREATE TABLE menu_item_labels (
    menu_item_id INT NOT NULL,
    label_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (menu_item_id, label_id),
    FOREIGN KEY (menu_item_id) REFERENCES kitchen_menu(item_id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES menu_labels(id) ON DELETE CASCADE,
    INDEX idx_label_id (label_id)
);
```

#### 7. Create payments table (already in schema, ensure it exists)
```sql
-- Verify payments table exists (should already be in schema)
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
```

#### 8. Create deliveries table (already in schema, ensure it exists)
```sql
-- Verify deliveries table exists (should already be in schema)
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
```

#### 9. Create reviews table (already in schema, ensure it exists)
```sql
-- Verify reviews table exists (should already be in schema)
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
```

---

## APPLICATION CODE CHANGES NEEDED

### Missing Entities to Create:
1. **Payment.java** - order-service or payment-service
2. **Delivery.java** - order-service or delivery-service
3. **Review.java** - menu-service or review-service
4. **SearchLog.java** (optional) - menu-service

### Entity Modifications:
1. **User.java** - Add address, city, state, postalCode, country, registrationDate fields
2. **Kitchen.java** - Add ownerUserId relationship, deliveryArea, cuisineTypes, approvalStatus (already has these)
3. **MenuItem.java** - Keep isVeg, isHalal, spicyLevel, rating (needs SQL support)
4. **OrderItem.java** - Add @CreatedDate private LocalDateTime createdAt field

---

## SUMMARY TABLE

| Aspect | Status | Action Required |
|--------|--------|-----------------|
| users table | ⚠️ Partial | Add address, city, state, postal_code, country, registration_date, role |
| kitchens table | ⚠️ Partial | Add owner_user_id, delivery_area, cuisine_types, approval_status |
| kitchen_menu table | ⚠️ Partial | Add is_veg, is_halal, spicy_level, rating |
| orders table | ✅ Complete | No changes needed |
| order_items table | ❌ Missing | Add created_at field |
| payments table | ✅ Complete | Already in schema, implement entity |
| deliveries table | ✅ Complete | Already in schema, implement entity |
| reviews table | ✅ Complete | Already in schema, implement entity |
| menu_labels table | ❌ Missing | Create new table |
| menu_item_labels junction | ❌ Missing | Create new junction table |
| search_logs table | ✅ Complete | Already in schema, optional implementation |

---

## EXECUTION ORDER

1. **Execute SQL ALTER statements** (Phase 1) - Fix existing tables
2. **Create new SQL tables** (Phase 2) - Add menu_labels and menu_item_labels
3. **Update Java entities** - Add missing fields to existing entities
4. **Create new Java entities** - Payment, Delivery, Review
5. **Create corresponding services** - PaymentService, DeliveryService, ReviewService
6. **Create repositories** - PaymentRepository, DeliveryRepository, ReviewRepository
7. **Create controllers** - PaymentController, DeliveryController, ReviewController
8. **Run Liquibase/Flyway migrations** - If using version control
9. **Test all endpoints** - Ensure compatibility

