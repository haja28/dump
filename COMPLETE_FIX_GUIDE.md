# COMPLETE DATABASE ALIGNMENT FIX GUIDE

**Status:** ✅ READY TO EXECUTE  
**Generated:** January 31, 2026  
**Last Updated:** After SQL syntax fixes

---

## OVERVIEW

You have **2 SQL script options** available to fix your database schema alignment issues:

1. **DATABASE_ALIGNMENT_FIX.sql** - Standard approach (fastest, direct execution)
2. **DATABASE_ALIGNMENT_FIX_WITH_ERROR_HANDLING.sql** - Procedure approach (safer, ignores duplicate errors)

Choose based on your needs below.

---

## 📋 WHAT'S BEING FIXED

Your analysis found **10 misalignments** between database and application code:

### Databases Tables Updated (4):
- ✅ **users** - Adding 7 new fields + role enum
- ✅ **kitchens** - Adding 4 new fields + relationships
- ✅ **kitchen_menu** - Adding 4 new fields for attributes
- ✅ **order_items** - Adding 1 missing timestamp field

### New Tables Created (2):
- ✅ **menu_labels** - For item categorization
- ✅ **menu_item_labels** - Junction table for relationships

### Tables Verified (4):
- ✅ **payments** - Already in schema
- ✅ **deliveries** - Already in schema
- ✅ **reviews** - Already in schema
- ✅ **search_logs** - Already in schema

---

## 🚀 QUICK START (Choose One Option)

### OPTION 1: Direct SQL Execution (Recommended for Clean Databases)

**Use this if:** Your database is new or you want direct execution without error handling.

**File:** `DATABASE_ALIGNMENT_FIX.sql`

**Command Line Execution:**
```bash
# Option A: Direct file input
mysql -u root -p database_name < DATABASE_ALIGNMENT_FIX.sql

# Option B: With password in command (less secure)
mysql -u root -pYourPassword database_name < DATABASE_ALIGNMENT_FIX.sql

# Option C: Via source inside MySQL client
mysql -u root -p database_name
mysql> SOURCE DATABASE_ALIGNMENT_FIX.sql;
```

**MySQL Workbench:**
1. Open File → Open SQL Script
2. Select `DATABASE_ALIGNMENT_FIX.sql`
3. Click Execute (Lightning bolt icon)

**DBeaver or Similar IDEs:**
1. Right-click on database → SQL Editor → Open SQL Script
2. Select `DATABASE_ALIGNMENT_FIX.sql`
3. Select all (Ctrl+A) and Execute (Ctrl+Enter)

---

### OPTION 2: Procedure with Error Handling (Recommended for Existing Databases)

**Use this if:** You've run migrations before or want idempotent execution.

**File:** `DATABASE_ALIGNMENT_FIX_WITH_ERROR_HANDLING.sql`

**Benefits:**
- ✅ Won't fail if columns already exist
- ✅ Continues on errors
- ✅ Safe to run multiple times
- ✅ Shows success message when done

**Command Line Execution:**
```bash
mysql -u root -p database_name < DATABASE_ALIGNMENT_FIX_WITH_ERROR_HANDLING.sql
```

---

## ⚠️ BEFORE YOU EXECUTE

### Prerequisites:
- [ ] MySQL 5.7 or higher installed
- [ ] Database already exists
- [ ] You have credentials to access the database
- [ ] You have backup of your database (recommended)
- [ ] All tables (users, kitchens, kitchen_menu, orders, order_items) already exist

### Backup Your Database:
```bash
# Create a backup first (strongly recommended)
mysqldump -u root -p database_name > database_backup_$(date +%Y%m%d_%H%M%S).sql
```

---

## 📊 WHAT GETS ADDED

### To USERS Table:
```sql
address (VARCHAR 255)
city (VARCHAR 50)
state (VARCHAR 50)
postal_code (VARCHAR 10)
country (VARCHAR 50)
registration_date (TIMESTAMP)
role (ENUM: CUSTOMER, KITCHEN, ADMIN)
```

### To KITCHENS Table:
```sql
owner_user_id (INT, Foreign Key to users)
delivery_area (VARCHAR 255)
cuisine_types (VARCHAR 500)
approval_status (ENUM: PENDING, APPROVED, REJECTED)
```

### To KITCHEN_MENU Table:
```sql
is_veg (BOOLEAN)
is_halal (BOOLEAN)
spicy_level (INT)
rating (DECIMAL)
```

### To ORDER_ITEMS Table:
```sql
created_at (TIMESTAMP)
```

### New Tables:
```sql
menu_labels - Menu item category labels
menu_item_labels - Junction table linking menu items to labels
```

---

## ⚡ EXECUTION STEPS

### Step 1: Connect to MySQL
```bash
# Open MySQL client
mysql -u root -p
# Enter your password when prompted
```

### Step 2: Select Your Database
```sql
USE your_database_name;
-- Replace 'your_database_name' with actual database name
```

### Step 3: Execute the Script

**Option A (Direct):**
```sql
SOURCE DATABASE_ALIGNMENT_FIX.sql;
```

**Option B (With Error Handling):**
```sql
SOURCE DATABASE_ALIGNMENT_FIX_WITH_ERROR_HANDLING.sql;
```

### Step 4: Verify Success
```sql
-- Check if all columns were added
DESC users;
DESC kitchens;
DESC kitchen_menu;
DESC order_items;

-- Check if new tables exist
SHOW TABLES LIKE 'menu_%';

-- Check foreign keys
SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'kitchens' AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## 🔍 TROUBLESHOOTING

### Error: "Table doesn't exist"
```
Error: Table 'database_name.users' doesn't exist
```
**Solution:** Make sure the base tables exist first. Run your original `database_schema.sql` first.

### Error: "Duplicate column name"
```
Error 1060: Duplicate column name 'address'
```
**Solution:** The column already exists (idempotent execution). This is normal if you've run the script before. Use **Option 2** (with error handling) to avoid this.

### Error: "Duplicate key name"
```
Error 1061: Duplicate key name 'idx_role'
```
**Solution:** Same as above - use Option 2 script for idempotent execution.

### Error: "Foreign key constraint fails"
```
Error 1216: Cannot add or update a child row
```
**Solution:** The `users` table might not exist or has issues. Verify base schema is initialized.

### Error: "Access denied"
```
Error 1045: Access denied for user 'root'@'localhost'
```
**Solution:** Check your username/password and user permissions.

---

## ✅ VERIFICATION QUERIES

After successful execution, run these to verify everything is correct:

### Check All New Columns Exist:
```sql
-- Users table
SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' AND TABLE_SCHEMA = DATABASE();

-- Kitchens table
SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'kitchens' AND TABLE_SCHEMA = DATABASE();

-- Kitchen_menu table
SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'kitchen_menu' AND TABLE_SCHEMA = DATABASE();

-- Order_items table
SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'order_items' AND TABLE_SCHEMA = DATABASE();
```

### Check New Tables Exist:
```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME IN ('menu_labels', 'menu_item_labels', 'payments', 'deliveries', 'reviews', 'search_logs');
```

### Check Indexes:
```sql
SHOW INDEX FROM users;
SHOW INDEX FROM kitchens;
SHOW INDEX FROM kitchen_menu;
```

### Check Foreign Keys:
```sql
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## 📝 NEXT STEPS AFTER DATABASE UPDATE

Once the database is updated successfully, you need to **update your Java application**:

### 1. Update Existing Entities

**File:** `auth-service/src/main/java/com/makanforyou/auth/entity/User.java`
```java
// Add these fields:
private String address;
private String city;
private String state;
private String postalCode;
private String country;
private LocalDateTime registrationDate;
```

**File:** `order-service/src/main/java/com/makanforyou/order/entity/OrderItem.java`
```java
// Add this field:
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
```

### 2. Create Missing Entities

Create these new files:
- `Payment.java` (for payments table)
- `Delivery.java` (for deliveries table)
- `Review.java` (for reviews table)

### 3. Create Services, Repositories, Controllers

For each new entity:
- Create `XxxService.java`
- Create `XxxRepository.java`
- Create `XxxController.java`

### 4. Update pom.xml Dependencies

Ensure you have proper dependency versions for your new services.

### 5. Test Your Application

```bash
# Build the application
mvn clean install

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

---

## 📚 REFERENCE FILES

Generated documentation files:
1. **ALIGNMENT_CHECK_SUMMARY.md** - Executive summary
2. **DATABASE_ALIGNMENT_ANALYSIS.md** - Detailed technical analysis
3. **DATABASE_vs_APPLICATION_DIFFERENCES.md** - Complete difference report
4. **DETAILED_COMPARISON_TABLE.md** - Field-by-field comparison
5. **SQL_QUICK_FIX.md** - Quick reference
6. **SQL_SYNTAX_FIX_REPORT.md** - Syntax corrections explained

---

## 🆘 GETTING HELP

### Common Issues:

**Q: Which script should I use?**
A: Use `DATABASE_ALIGNMENT_FIX.sql` if it's a fresh database, or `DATABASE_ALIGNMENT_FIX_WITH_ERROR_HANDLING.sql` if you've already run migrations.

**Q: Can I run the script multiple times?**
A: Only with the error-handling version (Option 2). Option 1 will fail with duplicate column errors on second run.

**Q: Do I need to restart my application?**
A: Yes, after updating both the database AND the Java code. Rebuild and restart the application services.

**Q: What if something goes wrong?**
A: Restore from your backup:
```bash
mysql -u root -p database_name < database_backup_YYYYMMDD_HHMMSS.sql
```

---

## 📋 EXECUTION CHECKLIST

- [ ] Database backed up
- [ ] Correct database name identified
- [ ] MySQL credentials ready
- [ ] Base schema already exists
- [ ] Chose between Option 1 or Option 2
- [ ] Executed the SQL script
- [ ] Ran verification queries
- [ ] All new columns present
- [ ] All new tables created
- [ ] Java entities updated
- [ ] Application rebuilt
- [ ] Application tested

---

## ✨ SUCCESS INDICATORS

After completion, you should see:

✅ All 7 new columns in users table  
✅ All 4 new columns in kitchens table  
✅ All 4 new columns in kitchen_menu table  
✅ created_at column in order_items table  
✅ menu_labels table exists  
✅ menu_item_labels table exists  
✅ All foreign key constraints established  
✅ All indexes created  
✅ Application starts without database errors  
✅ API endpoints work with new fields  

---

## 📞 SUPPORT

For issues, refer to:
- MySQL Error Codes: https://dev.mysql.com/doc/mysql-errors/8.0/en/
- Spring Boot JPA: https://spring.io/projects/spring-data-jpa
- MySQL Documentation: https://dev.mysql.com/doc/

