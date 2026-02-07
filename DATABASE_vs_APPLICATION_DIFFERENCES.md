# DATABASE vs APPLICATION ALIGNMENT REPORT
**Generated:** January 31, 2026

---

## EXECUTIVE SUMMARY

The application code is **NOT fully aligned** with the SQL database schema. There are:
- **10 misalignments** found in existing tables
- **2 missing tables** in SQL schema (required by application)
- **4 missing entities** in application code (should exist per schema)

---

## DETAILED DIFFERENCES

### 1. ❌ USERS TABLE MISALIGNMENT

#### Missing in Entity (SQL → Java):
```
- address (VARCHAR 255)
- city (VARCHAR 50)
- state (VARCHAR 50)
- postal_code (VARCHAR 10)
- country (VARCHAR 50)
- registration_date (TIMESTAMP)
```

#### Extra in Entity (Java → SQL):
```
- role (ENUM: CUSTOMER, KITCHEN, ADMIN)
  └─ Not in SQL schema; should be added for multi-role support
```

#### ALTER Statement:
```sql
ALTER TABLE users ADD COLUMN (
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE users ADD COLUMN role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') 
    DEFAULT 'CUSTOMER' AFTER password_hash;
ALTER TABLE users ADD INDEX idx_role (role);
```

---

### 2. ⚠️ KITCHENS TABLE MISALIGNMENT

#### Missing in Entity (SQL → Java):
```
- kitchen_owner_name (VARCHAR 100)
  └─ Entity uses ownerUserId foreign key instead
```

#### Extra in Entity (Java → SQL):
```
- owner_user_id (INT) - relationship to users table
- delivery_area (VARCHAR 255)
- cuisine_types (VARCHAR 500)
- approval_status (ENUM: PENDING, APPROVED, REJECTED)
```

#### Discrepancy:
```
- SQL has both old fields (kitchen_owner_*) and relationship approach
- Entity uses relationship approach (cleaner, but schema needs update)
```

#### ALTER Statement:
```sql
ALTER TABLE kitchens ADD COLUMN (
    owner_user_id INT,
    delivery_area VARCHAR(255),
    cuisine_types VARCHAR(500),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'
);
ALTER TABLE kitchens ADD CONSTRAINT fk_kitchens_owner_user_id 
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE kitchens ADD INDEX idx_owner_user_id (owner_user_id);
ALTER TABLE kitchens ADD INDEX idx_approval_status (approval_status);
```

---

### 3. ⚠️ KITCHEN_MENU TABLE MISALIGNMENT

#### Missing in SQL (Java → SQL):
```
- is_veg (BOOLEAN) - default TRUE
- is_halal (BOOLEAN) - default FALSE
- spicy_level (INT) - default 1
- rating (DECIMAL 3,2) - default 0.0
```

#### ALTER Statement:
```sql
ALTER TABLE kitchen_menu ADD COLUMN (
    is_veg BOOLEAN DEFAULT TRUE,
    is_halal BOOLEAN DEFAULT FALSE,
    spicy_level INT DEFAULT 1,
    rating DECIMAL(3, 2) DEFAULT 0.0
);
ALTER TABLE kitchen_menu ADD INDEX idx_is_veg (is_veg);
ALTER TABLE kitchen_menu ADD INDEX idx_rating (rating);
```

---

### 4. ❌ ORDER_ITEMS TABLE MISALIGNMENT

#### Missing in Entity (SQL → Java):
```
- created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
  └─ CRITICAL: This field exists in SQL but missing from OrderItem entity
```

#### ALTER Statement (Schema Fix):
```sql
ALTER TABLE order_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
```

#### Required Change (Entity Fix):
```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
```

---

### 5. ❌ MENU_LABELS TABLE - MISSING IN SQL

#### Status: Table exists in application but NOT in SQL schema

#### Entity Definition Found:
```
File: menu-service/src/main/java/com/makanforyou/menu/entity/MenuLabel.java
- id (INT, PK, AUTO_INCREMENT)
- name (VARCHAR 50, UNIQUE)
- description (VARCHAR 200)
- is_active (BOOLEAN, default TRUE)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### Required CREATE Statement:
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

---

### 6. ❌ MENU_ITEM_LABELS JUNCTION TABLE - MISSING IN SQL

#### Status: Junction table exists in application (@ManyToMany) but NOT in SQL schema

#### Relationship Details:
```
MenuItem (1) ←→ (Many) MenuLabel
via: @ManyToMany with join table "menu_item_labels"
```

#### Required CREATE Statement:
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

---

### 7. ❌ PAYMENTS TABLE - MISSING JAVA ENTITY

#### Status: Table exists in SQL schema but NO Java entity in application

#### SQL Table Exists:
```
File: database_schema.sql (lines 162-196)
Columns: payment_id, order_id, user_id, payment_amount, payment_method (ENUM),
         payment_status (ENUM), transaction_id, payment_date, refund_amount,
         refund_date, refund_reason, created_at, updated_at
```

#### Missing Java Entity:
```
Expected Location: payment-service/src/main/java/com/makanforyou/payment/entity/Payment.java
Status: DOES NOT EXIST
```

#### Action Required:
Create Payment entity and corresponding PaymentService, PaymentRepository, PaymentController

---

### 8. ❌ DELIVERIES TABLE - MISSING JAVA ENTITY

#### Status: Table exists in SQL schema but NO Java entity in application

#### SQL Table Exists:
```
File: database_schema.sql (lines 207-250)
Columns: delivery_id, order_id, kitchen_id, user_id, item_id, delivery_status (ENUM),
         assigned_to, pickup_time, delivery_time, estimated_delivery_time,
         current_location, delivery_notes, created_at, updated_at
```

#### Missing Java Entity:
```
Expected Location: order-service/src/main/java/com/makanforyou/order/entity/Delivery.java
Status: DOES NOT EXIST
```

#### Action Required:
Create Delivery entity and corresponding DeliveryService, DeliveryRepository, DeliveryController

---

### 9. ❌ REVIEWS TABLE - MISSING JAVA ENTITY

#### Status: Table exists in SQL schema but NO Java entity in application

#### SQL Table Exists:
```
File: database_schema.sql (lines 252-276)
Columns: review_id, order_id, user_id, kitchen_id, rating, review_text,
         review_date, helpful_count
```

#### Missing Java Entity:
```
Expected Location: menu-service/src/main/java/com/makanforyou/menu/entity/Review.java
                   OR order-service/src/main/java/com/makanforyou/order/entity/Review.java
Status: DOES NOT EXIST
```

#### Action Required:
Create Review entity and corresponding ReviewService, ReviewRepository, ReviewController

---

### 10. ❌ SEARCH_LOGS TABLE - MISSING JAVA ENTITY (OPTIONAL)

#### Status: Table exists in SQL schema but NO Java entity in application

#### SQL Table Exists:
```
File: database_schema.sql (lines 278-291)
Columns: search_id, user_id, search_query, search_date, results_count
```

#### Missing Java Entity:
```
Status: OPTIONAL - Not critical for core functionality
```

---

## ALIGNMENT MATRIX

| Table | SQL | Entity | Aligned | Status | Priority |
|-------|-----|--------|---------|--------|----------|
| users | ✅ | ✅ | ⚠️ PARTIAL | Missing 6 fields + add role | HIGH |
| kitchens | ✅ | ✅ | ⚠️ PARTIAL | Missing 4 fields | HIGH |
| kitchen_menu | ✅ | ✅ | ⚠️ PARTIAL | Missing 4 fields in SQL | HIGH |
| orders | ✅ | ✅ | ✅ COMPLETE | No changes needed | - |
| order_items | ✅ | ✅ | ❌ MISSING | Missing createdAt in entity | HIGH |
| menu_labels | ❌ | ✅ | ❌ MISSING | Table not in SQL | MEDIUM |
| menu_item_labels | ❌ | ✅ | ❌ MISSING | Junction table not in SQL | MEDIUM |
| payments | ✅ | ❌ | ❌ MISSING | Entity not in app | HIGH |
| deliveries | ✅ | ❌ | ❌ MISSING | Entity not in app | HIGH |
| reviews | ✅ | ❌ | ❌ MISSING | Entity not in app | MEDIUM |
| search_logs | ✅ | ❌ | ❌ MISSING | Entity optional | LOW |

---

## PRIORITIZED ACTION PLAN

### PRIORITY 1: CRITICAL (Execute First)
1. **Add fields to users table** - Support for address, location, role
2. **Add fields to kitchens table** - Support for owner relationship, delivery area, approval status
3. **Add fields to kitchen_menu table** - Support for dietary attributes and ratings
4. **Add createdAt to order_items** - Required by schema
5. **Update OrderItem entity** - Add missing createdAt field

### PRIORITY 2: HIGH (Execute Second)
6. **Create menu_labels table** - Support menu item categories
7. **Create menu_item_labels junction table** - Support menu item to label relationship
8. **Create Payment entity** - Payment processing support
9. **Create Delivery entity** - Delivery tracking support
10. **Create PaymentService, DeliveryService, repositories & controllers**

### PRIORITY 3: MEDIUM (Execute Third)
11. **Create Review entity** - Review functionality
12. **Create ReviewService, repository & controller**

### PRIORITY 4: LOW (Optional)
13. **Create SearchLog entity** - Optional logging functionality

---

## IMPLEMENTATION CHECKLIST

### Database Changes (SQL):
- [ ] Execute database_schema.sql (original schema)
- [ ] Execute DATABASE_ALIGNMENT_FIX.sql (updates)
- [ ] Verify all tables exist
- [ ] Verify all indexes created
- [ ] Verify foreign keys established

### Application Code Changes:

#### Entity Updates:
- [ ] User.java - Add address, city, state, postalCode, country, registrationDate
- [ ] OrderItem.java - Add createdAt field

#### New Entities to Create:
- [ ] Payment.java
- [ ] Delivery.java
- [ ] Review.java

#### New Services:
- [ ] PaymentService
- [ ] DeliveryService
- [ ] ReviewService
- [ ] (Optional) SearchLogService

#### New Repositories:
- [ ] PaymentRepository
- [ ] DeliveryRepository
- [ ] ReviewRepository
- [ ] (Optional) SearchLogRepository

#### New Controllers:
- [ ] PaymentController
- [ ] DeliveryController
- [ ] ReviewController

---

## BACKWARD COMPATIBILITY NOTES

1. **Kitchen Table Migration:**
   - Old fields (kitchen_owner_name, kitchen_owner_contact, kitchen_owner_email) can coexist with new owner_user_id
   - Consider data migration strategy before dropping old fields
   - Recommend keeping old fields for backward compatibility initially

2. **User Table Migration:**
   - Adding role field requires default value (CUSTOMER)
   - Existing users will need role assignment strategy

3. **MenuItem Table:**
   - New boolean/enum fields have safe defaults (won't break existing queries)

---

## TESTING RECOMMENDATIONS

After applying all changes:

1. **Unit Tests:**
   - Test all entity field mappings
   - Test ORM annotations
   - Test repository queries

2. **Integration Tests:**
   - Test all CRUD operations
   - Test foreign key constraints
   - Test cascade operations

3. **API Tests:**
   - Test all endpoints with new fields
   - Test validation
   - Test error handling

4. **Database Tests:**
   - Test indexes on query performance
   - Test unique constraints
   - Test check constraints (e.g., rating between 1-5)

---

## FILES GENERATED

1. **DATABASE_ALIGNMENT_ANALYSIS.md** - Detailed analysis (this file)
2. **DATABASE_ALIGNMENT_FIX.sql** - Executable SQL statements
3. **DATABASE_vs_APPLICATION_DIFFERENCES.md** - This report

---

## CONCLUSION

The application and database are **not fully synchronized**. The provided SQL fix script will bring the database into alignment with the application code. However, new Java entities (Payment, Delivery, Review) still need to be created to fully utilize the database schema.

Estimated effort to complete alignment: **2-3 hours** for an experienced developer.

