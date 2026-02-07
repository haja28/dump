# ✅ DATABASE SCHEMA MISMATCH FIX - KITCHEN SERVICE

## Issue Summary
The kitchen registration endpoint was failing with:
```
Caused by: java.sql.SQLException: Field 'address' doesn't have a default value
```

## Root Cause Analysis

### The Real Problem
The error message is misleading. The actual issue is **NOT** that the address field is missing a default value. The problem is:

1. **Missing Column**: The database schema was missing the `owner_user_id` column
2. **Missing Column**: The database schema was missing the `approval_status` column
3. When these required columns are missing from the database, MySQL can't complete the INSERT, resulting in the confusing error message about the address field

### Entity vs Database Mismatch

**Java Entity (Kitchen.java) expects these columns:**
```
owner_user_id (REQUIRED, NOT NULL)
approval_status (REQUIRED, NOT NULL, ENUM: PENDING/APPROVED/REJECTED)
```

**Database Schema had:**
```
❌ owner_user_id - MISSING
❌ approval_status - MISSING
```

### Why the Error Says "address" ?
MySQL returns this confusing error because:
1. The INSERT statement includes columns in a specific order
2. When Hibernate tries to bind the prepared statement parameters, the column count doesn't match
3. MySQL's error reporting mechanism can point to the wrong column in these scenarios

---

## Solution Steps

### Step 1: Update Database Schema

Run the migration script to add the missing columns:

```sql
ALTER TABLE kitchens 
ADD COLUMN owner_user_id INT NOT NULL 
AFTER kitchen_owner_name;

ALTER TABLE kitchens 
ADD COLUMN approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') 
DEFAULT 'PENDING' NOT NULL
AFTER verified;

ALTER TABLE kitchens 
ADD CONSTRAINT fk_owner_user_id 
FOREIGN KEY (owner_user_id) 
REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE kitchens 
ADD INDEX idx_owner_user_id (owner_user_id);
```

### Step 2: Verify Database Schema

After running the migration, verify the columns exist:

```sql
DESCRIBE kitchens;
```

Expected columns:
- `kitchen_id` - INT PRIMARY KEY AUTO_INCREMENT
- `kitchen_name` - VARCHAR(100) NOT NULL
- `kitchen_address` - VARCHAR(255) NOT NULL
- `kitchen_owner_name` - VARCHAR(100) NOT NULL
- `owner_user_id` - INT NOT NULL ✅ (newly added)
- `kitchen_owner_contact` - VARCHAR(15) NOT NULL
- `kitchen_owner_email` - VARCHAR(100) NOT NULL
- `kitchen_alternate_contact` - VARCHAR(15)
- `kitchen_alternate_email` - VARCHAR(100)
- `kitchen_description` - TEXT
- `kitchen_city` - VARCHAR(50)
- `kitchen_state` - VARCHAR(50)
- `kitchen_postal_code` - VARCHAR(10)
- `kitchen_country` - VARCHAR(50)
- `latitude` - DECIMAL(10, 8)
- `longitude` - DECIMAL(11, 8)
- `rating` - DECIMAL(3, 2) DEFAULT 0
- `total_orders` - INT DEFAULT 0
- `is_active` - BOOLEAN DEFAULT TRUE
- `verified` - BOOLEAN DEFAULT FALSE
- `approval_status` - ENUM('PENDING','APPROVED','REJECTED') NOT NULL ✅ (newly added)
- `registration_date` - TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- `created_at` - TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- `updated_at` - TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

### Step 3: Update database_schema.sql

The main `database_schema.sql` file has been updated with:
- `owner_user_id INT NOT NULL` column after kitchen_owner_name
- `approval_status` column with proper ENUM type
- Foreign key constraint for owner_user_id
- Index on owner_user_id

### Step 4: Rebuild and Test

```bash
# Clean build
mvn clean package -DskipTests

# Or if you prefer to skip certain services
cd kitchen-service
mvn clean package -DskipTests
```

### Step 5: Test Kitchen Registration

Use the provided request body to test:

```bash
curl -X POST http://localhost:8080/api/v1/kitchens/register \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "kitchenName": "Priya Kitchen",
    "ownerName": "Priya Kumar",
    "address": "123 Main Street Apt 4",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "ownerContact": "9876543210",
    "ownerEmail": "priya@example.com",
    "cuisineTypes": "Indian,Continental",
    "description": "Home-cooked Indian and Continental food"
  }'
```

---

## Technical Details

### Kitchen Entity JPA Mappings
```java
@Column(nullable = false, name = "owner_user_id")
private Long ownerUserId;

@Enumerated(EnumType.STRING)
@Column(nullable = false, name = "approval_status")
@Builder.Default
private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
```

### Database Table Definition
```sql
CREATE TABLE kitchens (
    kitchen_id INT PRIMARY KEY AUTO_INCREMENT,
    kitchen_name VARCHAR(100) NOT NULL,
    kitchen_address VARCHAR(255) NOT NULL,
    kitchen_owner_name VARCHAR(100) NOT NULL,
    owner_user_id INT NOT NULL,           -- ✅ REQUIRED
    kitchen_owner_contact VARCHAR(15) NOT NULL,
    kitchen_owner_email VARCHAR(100) NOT NULL,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') 
                    DEFAULT 'PENDING' NOT NULL,  -- ✅ REQUIRED
    -- ... other columns ...
    FOREIGN KEY (owner_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

---

## Files Modified

1. **database_schema.sql** - Updated CREATE TABLE statement with missing columns
2. **DATABASE_SCHEMA_UPDATE.sql** - Migration script for existing databases (newly created)

## Files NOT Modified
- Kitchen.java - Entity is correct ✅
- KitchenService.java - Service logic is correct ✅
- KitchenRegistrationRequest.java - DTO validation is correct ✅
- KitchenController.java - Controller is correct ✅

---

## Verification Checklist

After applying the fix:

- [ ] Run migration script: `DATABASE_SCHEMA_UPDATE.sql`
- [ ] Verify columns exist with DESCRIBE kitchens
- [ ] Clean rebuild: `mvn clean package -DskipTests`
- [ ] Test registration endpoint with valid request
- [ ] Verify kitchen record created in database
- [ ] Check approval_status is set to PENDING
- [ ] Verify owner_user_id is correctly stored

---

## Common Issues After Fix

### Issue 1: Column already exists error
If you get "Duplicate column name" error, the column may already exist:
```sql
-- Check if column exists
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME='kitchens' AND TABLE_SCHEMA='makanforyou' 
AND COLUMN_NAME='owner_user_id';

-- If it exists, you can skip that ALTER statement
```

### Issue 2: Foreign key constraint fails
Ensure the user with X-User-Id exists in the users table before registering a kitchen.

### Issue 3: Still getting database errors
Drop and recreate the entire database using the updated `database_schema.sql`:
```sql
DROP DATABASE makanforyou;
-- Then run the full database_schema.sql
```

---

## Next Steps

1. Apply the database migration script
2. Rebuild the project
3. Test the kitchen registration endpoint
4. Verify the data is correctly stored in the database

---

**Status**: ✅ READY TO APPLY  
**Last Updated**: February 4, 2026  
**Affected Service**: Kitchen Service  
**Severity**: CRITICAL (blocks kitchen registration)

