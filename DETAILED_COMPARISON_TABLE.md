# DATABASE SCHEMA vs APPLICATION ENTITY - DETAILED COMPARISON

Generated: January 31, 2026

---

## TABLE 1: USERS

### SQL Schema Definition:
```sql
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Java Entity (User.java):
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // ↔ user_id
    
    private String firstName;                  // ↔ first_name
    private String lastName;                   // ↔ last_name
    private String email;                      // ↔ email
    private String phoneNumber;                // ↔ phone_number
    private String passwordHash;               // ↔ password_hash
    
    // ❌ MISSING FROM ENTITY - Should exist:
    // private String address;                 // ← address
    // private String city;                    // ← city
    // private String state;                   // ← state
    // private String postalCode;              // ← postal_code
    // private String country;                 // ← country
    // private LocalDateTime registrationDate; // ← registration_date
    
    private UserRole role;                     // ✅ EXTRA - Not in SQL (SHOULD ADD)
    private Boolean isActive;                  // ↔ is_active
    private LocalDateTime createdAt;           // ↔ created_at
    private LocalDateTime updatedAt;           // ↔ updated_at
    private LocalDateTime lastLogin;           // ↔ last_login
}
```

### Alignment Report:
| SQL Field | Java Field | Status | Notes |
|-----------|-----------|--------|-------|
| user_id | id | ✅ | Mapped correctly |
| first_name | firstName | ✅ | Mapped correctly |
| last_name | lastName | ✅ | Mapped correctly |
| email | email | ✅ | Mapped correctly |
| phone_number | phoneNumber | ✅ | Mapped correctly |
| password_hash | passwordHash | ✅ | Mapped correctly |
| address | - | ❌ | MISSING in entity |
| city | - | ❌ | MISSING in entity |
| state | - | ❌ | MISSING in entity |
| postal_code | - | ❌ | MISSING in entity |
| country | - | ❌ | MISSING in entity |
| registration_date | - | ❌ | MISSING in entity |
| last_login | lastLogin | ✅ | Mapped correctly |
| is_active | isActive | ✅ | Mapped correctly |
| created_at | createdAt | ✅ | Mapped correctly |
| updated_at | updatedAt | ✅ | Mapped correctly |
| - | role | ❌ | EXTRA in entity (not in SQL) |

### ✅ FIX: Add to users table
```sql
ALTER TABLE users ADD COLUMN (
    role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') DEFAULT 'CUSTOMER' AFTER password_hash
);
```

### ✅ FIX: Update User entity
```java
private String address;
private String city;
private String state;
private String postalCode;
private String country;
private LocalDateTime registrationDate;
```

---

## TABLE 2: KITCHENS

### SQL Schema Definition (Excerpt):
```sql
CREATE TABLE kitchens (
    kitchen_id INT PRIMARY KEY AUTO_INCREMENT,
    kitchen_name VARCHAR(100) NOT NULL,
    kitchen_address VARCHAR(255) NOT NULL,
    kitchen_owner_name VARCHAR(100) NOT NULL,
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
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Java Entity (Kitchen.java):
```java
@Entity
@Table(name = "kitchens")
public class Kitchen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // ↔ kitchen_id
    
    private String kitchenName;                // ↔ kitchen_name
    private Long ownerUserId;                  // ❌ NOT IN SQL (but in entity)
    private String description;                // ↔ kitchen_description
    private String address;                    // ↔ kitchen_address
    private String city;                       // ↔ kitchen_city
    private String state;                      // ↔ kitchen_state
    private String postalCode;                 // ↔ kitchen_postal_code
    private String country;                    // ↔ kitchen_country
    private Double latitude;                   // ↔ latitude
    private Double longitude;                  // ↔ longitude
    private String deliveryArea;               // ❌ NOT IN SQL (but in entity)
    private String cuisineTypes;               // ❌ NOT IN SQL (but in entity)
    private String ownerContact;               // ↔ kitchen_owner_contact
    private String ownerEmail;                 // ↔ kitchen_owner_email
    private String alternateContact;           // ↔ kitchen_alternate_contact
    private String alternateEmail;             // ↔ kitchen_alternate_email
    private ApprovalStatus approvalStatus;     // ❌ NOT IN SQL (but in entity)
    private Double rating;                     // ↔ rating
    private Integer totalOrders;               // ↔ total_orders
    private Boolean isActive;                  // ↔ is_active
    private Boolean verified;                  // ↔ verified
    
    // ❌ MISSING FROM ENTITY:
    // kitchen_owner_name
    
    private LocalDateTime createdAt;           // ↔ created_at
    private LocalDateTime updatedAt;           // ↔ updated_at
}
```

### Alignment Report:
| SQL Field | Java Field | Status | Notes |
|-----------|-----------|--------|-------|
| kitchen_id | id | ✅ | Mapped correctly |
| kitchen_name | kitchenName | ✅ | Mapped correctly |
| kitchen_address | address | ✅ | Mapped correctly |
| kitchen_owner_name | - | ❌ | MISSING in entity |
| kitchen_owner_contact | ownerContact | ✅ | Mapped correctly |
| kitchen_owner_email | ownerEmail | ✅ | Mapped correctly |
| kitchen_alternate_contact | alternateContact | ✅ | Mapped correctly |
| kitchen_alternate_email | alternateEmail | ✅ | Mapped correctly |
| kitchen_description | description | ✅ | Mapped correctly |
| kitchen_city | city | ✅ | Mapped correctly |
| kitchen_state | state | ✅ | Mapped correctly |
| kitchen_postal_code | postalCode | ✅ | Mapped correctly |
| kitchen_country | country | ✅ | Mapped correctly |
| latitude | latitude | ✅ | Mapped correctly |
| longitude | longitude | ✅ | Mapped correctly |
| rating | rating | ✅ | Mapped correctly |
| total_orders | totalOrders | ✅ | Mapped correctly |
| is_active | isActive | ✅ | Mapped correctly |
| verified | verified | ✅ | Mapped correctly |
| registration_date | - | ❓ | Not explicitly mapped |
| created_at | createdAt | ✅ | Mapped correctly |
| updated_at | updatedAt | ✅ | Mapped correctly |
| - | ownerUserId | ❌ | EXTRA in entity (not in SQL) |
| - | deliveryArea | ❌ | EXTRA in entity (not in SQL) |
| - | cuisineTypes | ❌ | EXTRA in entity (not in SQL) |
| - | approvalStatus | ❌ | EXTRA in entity (not in SQL) |

### ✅ FIX: Add to kitchens table
```sql
ALTER TABLE kitchens ADD COLUMN (
    owner_user_id INT,
    delivery_area VARCHAR(255),
    cuisine_types VARCHAR(500),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'
);
ALTER TABLE kitchens ADD CONSTRAINT fk_kitchens_owner_user_id 
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
```

---

## TABLE 3: KITCHEN_MENU

### SQL Schema Definition (Excerpt):
```sql
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Java Entity (MenuItem.java):
```java
@Entity
@Table(name = "kitchen_menu")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // ↔ item_id
    
    private Long kitchenId;                    // ↔ kitchen_id
    private String itemName;                   // ↔ item_name
    private String description;                // ↔ item_description
    private String ingredients;                // ↔ item_ingredients
    private String allergyIndication;          // ↔ item_allergic_indication
    private BigDecimal cost;                   // ↔ item_cost
    private String imagePath;                  // ↔ item_image_path
    private String availableTiming;            // ↔ item_available_timing
    private Boolean isActive;                  // ↔ item_is_active
    private Integer preparationTimeMinutes;    // ↔ preparation_time_minutes
    private Integer quantityAvailable;         // ↔ item_quantity_available
    
    // ❌ NOT IN SQL (but in entity):
    private Boolean isVeg;                     // ← New field
    private Boolean isHalal;                   // ← New field
    private Integer spicyLevel;                // ← New field
    private Double rating;                     // ← New field
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(...)
    private Set<MenuLabel> labels;             // ← NEW RELATIONSHIP (junction table missing)
    
    private LocalDateTime createdAt;           // ↔ created_at
    private LocalDateTime updatedAt;           // ↔ updated_at
}
```

### Alignment Report:
| SQL Field | Java Field | Status | Notes |
|-----------|-----------|--------|-------|
| item_id | id | ✅ | Mapped correctly |
| kitchen_id | kitchenId | ✅ | Mapped correctly |
| item_name | itemName | ✅ | Mapped correctly |
| item_description | description | ✅ | Mapped correctly |
| item_ingredients | ingredients | ✅ | Mapped correctly |
| item_allergic_indication | allergyIndication | ✅ | Mapped correctly |
| item_cost | cost | ✅ | Mapped correctly |
| item_image_path | imagePath | ✅ | Mapped correctly |
| item_available_timing | availableTiming | ✅ | Mapped correctly |
| item_is_active | isActive | ✅ | Mapped correctly |
| preparation_time_minutes | preparationTimeMinutes | ✅ | Mapped correctly |
| item_quantity_available | quantityAvailable | ✅ | Mapped correctly |
| created_at | createdAt | ✅ | Mapped correctly |
| updated_at | updatedAt | ✅ | Mapped correctly |
| - | isVeg | ❌ | EXTRA in entity (not in SQL) |
| - | isHalal | ❌ | EXTRA in entity (not in SQL) |
| - | spicyLevel | ❌ | EXTRA in entity (not in SQL) |
| - | rating | ❌ | EXTRA in entity (not in SQL) |
| - | labels (relationship) | ❌ | EXTRA: Junction table missing |

### ✅ FIX: Add to kitchen_menu table
```sql
ALTER TABLE kitchen_menu ADD COLUMN (
    is_veg BOOLEAN DEFAULT TRUE,
    is_halal BOOLEAN DEFAULT FALSE,
    spicy_level INT DEFAULT 1,
    rating DECIMAL(3, 2) DEFAULT 0.0
);
```

### ✅ FIX: Create missing tables
```sql
CREATE TABLE menu_labels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE menu_item_labels (
    menu_item_id INT NOT NULL,
    label_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (menu_item_id, label_id),
    FOREIGN KEY (menu_item_id) REFERENCES kitchen_menu(item_id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES menu_labels(id) ON DELETE CASCADE
);
```

---

## TABLE 4: ORDERS

### SQL Schema Definition (Excerpt):
```sql
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Java Entity (Order.java):
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // ↔ order_id
    
    private Long userId;                       // ↔ user_id
    private Long kitchenId;                    // ↔ kitchen_id
    private BigDecimal orderTotal;             // ↔ order_total
    private OrderStatus orderStatus;           // ↔ order_status
    private Boolean confirmationByKitchen;     // ↔ confirmation_by_kitchen
    private LocalDateTime confirmationTimestamp; // ↔ confirmation_timestamp
    private String deliveryAddress;            // ↔ delivery_address
    private String deliveryCity;               // ↔ delivery_city
    private String deliveryState;              // ↔ delivery_state
    private String deliveryPostalCode;         // ↔ delivery_postal_code
    private String specialInstructions;        // ↔ special_instructions
    private LocalDateTime createdAt;           // ↔ created_at
    private LocalDateTime updatedAt;           // ↔ updated_at
    
    // ❌ Note: SQL has order_date, entity uses createdAt instead
    //    This is acceptable pattern, but slight discrepancy exists
}
```

### Alignment Report:
| SQL Field | Java Field | Status | Notes |
|-----------|-----------|--------|-------|
| order_id | id | ✅ | Mapped correctly |
| user_id | userId | ✅ | Mapped correctly |
| kitchen_id | kitchenId | ✅ | Mapped correctly |
| order_date | createdAt | ⚠️ | Different field names but same purpose |
| order_total | orderTotal | ✅ | Mapped correctly |
| order_status | orderStatus | ✅ | Mapped correctly (ENUM) |
| confirmation_by_kitchen | confirmationByKitchen | ✅ | Mapped correctly |
| confirmation_timestamp | confirmationTimestamp | ✅ | Mapped correctly |
| delivery_address | deliveryAddress | ✅ | Mapped correctly |
| delivery_city | deliveryCity | ✅ | Mapped correctly |
| delivery_state | deliveryState | ✅ | Mapped correctly |
| delivery_postal_code | deliveryPostalCode | ✅ | Mapped correctly |
| special_instructions | specialInstructions | ✅ | Mapped correctly |
| created_at | createdAt | ✅ | Mapped correctly |
| updated_at | updatedAt | ✅ | Mapped correctly |

### Status: ✅ FULLY ALIGNED

---

## TABLE 5: ORDER_ITEMS

### SQL Schema Definition:
```sql
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    item_quantity INT NOT NULL,
    item_unit_price DECIMAL(10, 2) NOT NULL,
    item_total DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Java Entity (OrderItem.java):
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // ↔ order_item_id
    
    private Long orderId;                      // ↔ order_id
    private Long itemId;                       // ↔ item_id
    private Integer itemQuantity;              // ↔ item_quantity
    private BigDecimal itemUnitPrice;          // ↔ item_unit_price
    private BigDecimal itemTotal;              // ↔ item_total
    private String specialRequests;            // ↔ special_requests
    
    // ❌ MISSING FROM ENTITY:
    // private LocalDateTime createdAt;        // ← created_at (REQUIRED BY SQL!)
}
```

### Alignment Report:
| SQL Field | Java Field | Status | Notes |
|-----------|-----------|--------|-------|
| order_item_id | id | ✅ | Mapped correctly |
| order_id | orderId | ✅ | Mapped correctly |
| item_id | itemId | ✅ | Mapped correctly |
| item_quantity | itemQuantity | ✅ | Mapped correctly |
| item_unit_price | itemUnitPrice | ✅ | Mapped correctly |
| item_total | itemTotal | ✅ | Mapped correctly |
| special_requests | specialRequests | ✅ | Mapped correctly |
| created_at | - | ❌ | CRITICAL: MISSING in entity |

### Status: ❌ MISALIGNED - MISSING CRITICAL FIELD

### ✅ FIX: Update OrderItem entity
```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
```

---

## SUMMARY OF ALL DIFFERENCES

### Missing Fields in Entities (Need to Add):
1. **User entity:**
   - address, city, state, postalCode, country, registrationDate

2. **OrderItem entity:**
   - createdAt

### Missing Fields in Database (Need to ALTER):
1. **users table:**
   - role (ENUM)

2. **kitchens table:**
   - owner_user_id (INT, FK)
   - delivery_area (VARCHAR)
   - cuisine_types (VARCHAR)
   - approval_status (ENUM)

3. **kitchen_menu table:**
   - is_veg (BOOLEAN)
   - is_halal (BOOLEAN)
   - spicy_level (INT)
   - rating (DECIMAL)

### Missing Tables in Database:
1. **menu_labels** - Required by MenuItem entity
2. **menu_item_labels** - Junction table for MenuItem-MenuLabel relationship

### Missing Entities in Application:
1. **Payment** - SQL table exists, no Java entity
2. **Delivery** - SQL table exists, no Java entity
3. **Review** - SQL table exists, no Java entity

---

## TOTAL MISALIGNMENTS: 10

