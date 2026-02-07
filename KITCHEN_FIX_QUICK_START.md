# 🔧 QUICK FIX: Kitchen Registration Database Error

## The Problem
```
java.sql.SQLException: Field 'address' doesn't have a default value
```

## The Real Issue
❌ Missing columns in database:
- `owner_user_id` 
- `approval_status`

---

## 🚀 Quick Fix (3 Steps)

### Step 1: Run Migration Script
Execute `DATABASE_SCHEMA_UPDATE.sql` in your MySQL client:

```sql
-- Copy and paste the entire content of DATABASE_SCHEMA_UPDATE.sql
-- and run it against your makanforyou database
```

**OR** run these individual commands:

```sql
USE makanforyou;

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

### Step 2: Verify Changes
```sql
DESCRIBE kitchens;
```

Should see:
- ✅ owner_user_id column
- ✅ approval_status column

### Step 3: Rebuild & Test
```bash
mvn clean package -DskipTests
```

---

## Test Request

```bash
curl -X POST http://localhost:8080/api/v1/kitchens/register \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "kitchenName": "Test Kitchen",
    "ownerName": "Test Owner",
    "address": "123 Main Street",
    "ownerContact": "9876543210",
    "ownerEmail": "test@example.com"
  }'
```

---

## Expected Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "kitchenName": "Test Kitchen",
    "ownerUserId": 1,
    "ownerName": "Test Owner",
    "address": "123 Main Street",
    "approvalStatus": "PENDING",
    ...
  },
  "message": "Kitchen registered successfully"
}
```

---

## ✅ Status
- Database: Schema mismatch detected and fixed
- Entity: Correct (no changes needed)
- Service: Correct (no changes needed)
- Ready to test: YES

