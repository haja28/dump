# ✅ KITCHEN REGISTRATION - ADDRESS FIELD FIX

## Issue
The kitchen registration endpoint was failing with:
```
Field 'address' doesn't have a default value
```

## Root Cause
The `address` field is **REQUIRED** and **NOT NULL** in the database, but was not being provided in the test request.

## Solution
Include the `address` field in the kitchen registration request with a valid value.

---

## ✅ Correct Request Format

### Required Fields
- `kitchenName` - Name of the kitchen (3-100 characters)
- `ownerName` - Name of the kitchen owner (2-100 characters) 
- `address` - Kitchen address (5-255 characters) **← REQUIRED**
- `ownerContact` - Owner phone number (10-15 digits)
- `ownerEmail` - Owner email address (valid email format)

### Optional Fields
- `description` - Kitchen description (max 255 characters)
- `city` - City name
- `state` - State/Province
- `postalCode` - Postal code
- `country` - Country name
- `latitude` - Latitude coordinate
- `longitude` - Longitude coordinate
- `deliveryArea` - Delivery coverage area
- `cuisineTypes` - Types of cuisine offered
- `alternateContact` - Alternate phone number
- `alternateEmail` - Alternate email address

---

## Complete Valid Request

```json
{
  "kitchenName": "Priya's Kitchen",
  "ownerName": "Priya Kumar",
  "address": "123 Main Street, Apartment 4",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "ownerContact": "9876543210",
  "ownerEmail": "priya@example.com",
  "alternateContact": "9876543211",
  "alternateEmail": "priya.alt@example.com",
  "cuisineTypes": "Indian,Continental",
  "deliveryArea": "Manhattan, Brooklyn",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "description": "Home-cooked Indian and Continental food prepared fresh daily"
}
```

---

## Minimal Valid Request

```json
{
  "kitchenName": "Priya's Kitchen",
  "ownerName": "Priya Kumar",
  "address": "123 Main Street, Apt 4",
  "ownerContact": "9876543210",
  "ownerEmail": "priya@example.com"
}
```

---

## Test with cURL

### Using Minimal Fields
```bash
curl -X POST http://localhost:8080/api/v1/kitchens/register \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "kitchenName": "Priya Kitchen",
    "ownerName": "Priya Kumar",
    "address": "123 Main Street Apt 4",
    "ownerContact": "9876543210",
    "ownerEmail": "priya@example.com"
  }'
```

### Using All Fields
```bash
curl -X POST http://localhost:8080/api/v1/kitchens/register \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "kitchenName": "Priya Kitchen",
    "ownerName": "Priya Kumar",
    "address": "123 Main Street Apartment 4",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "ownerContact": "9876543210",
    "ownerEmail": "priya@example.com",
    "alternateContact": "9876543211",
    "alternateEmail": "priya.alt@example.com",
    "cuisineTypes": "Indian,Continental",
    "deliveryArea": "Manhattan, Brooklyn",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "description": "Home-cooked Indian and Continental food"
  }'
```

---

## Expected Success Response (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "kitchenName": "Priya Kitchen",
    "ownerUserId": 1,
    "ownerName": "Priya Kumar",
    "address": "123 Main Street Apartment 4",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "deliveryArea": "Manhattan, Brooklyn",
    "cuisineTypes": "Indian,Continental",
    "ownerContact": "9876543210",
    "ownerEmail": "priya@example.com",
    "alternateContact": "9876543211",
    "alternateEmail": "priya.alt@example.com",
    "description": "Home-cooked Indian and Continental food",
    "approvalStatus": "PENDING",
    "rating": 0.0,
    "totalOrders": 0,
    "isActive": true,
    "verified": false,
    "createdAt": "2026-02-04T10:30:00Z",
    "updatedAt": "2026-02-04T10:30:00Z"
  },
  "message": "Kitchen registered successfully"
}
```

---

## Validation Rules

### kitchenName
- Required: ✅ YES (@NotBlank)
- Min Length: 3 characters
- Max Length: 100 characters
- Example: "Priya's Kitchen"

### ownerName
- Required: ✅ YES (@NotBlank)
- Min Length: 2 characters
- Max Length: 100 characters
- Example: "Priya Kumar"

### address
- Required: ✅ YES (@NotBlank)
- Min Length: 5 characters
- Max Length: 255 characters
- Example: "123 Main Street, Apartment 4"
- **NOTE**: This is the field that was missing!

### ownerContact
- Required: ✅ YES (@NotBlank)
- Format: 10-15 digits (phone number)
- Example: "9876543210"

### ownerEmail
- Required: ✅ YES (@NotBlank)
- Format: Valid email address
- Example: "priya@example.com"

### Optional Fields
- city: Max 50 characters
- state: Max 50 characters
- postalCode: Max 10 characters
- country: Max 50 characters
- cuisineTypes: Max 500 characters
- deliveryArea: Max 255 characters
- description: Max 255 characters
- latitude: Between -90 and 90
- longitude: Between -180 and 180

---

## Common Errors & Solutions

### Error 1: Field 'address' doesn't have a default value
```
Cause: address field is missing or null in request
Solution: Include "address" field with a valid value (5-255 characters)
```

### Error 2: Address is required
```
Cause: address field is empty or whitespace
Solution: Provide a non-empty address string
```

### Error 3: Address must be between 5 and 255 characters
```
Cause: address is too short (< 5 chars) or too long (> 255 chars)
Solution: Use an address between 5-255 characters
Example Valid: "123 Main St"
Example Invalid: "123" (too short)
```

### Error 4: 400 Bad Request
```
Cause: Missing required field or invalid format
Solution: Check all required fields are present and valid
Required: kitchenName, ownerName, address, ownerContact, ownerEmail
```

---

## Database Schema Verification

The `address` column in the `kitchens` table is defined as:
```sql
kitchen_address VARCHAR(255) NOT NULL
```

This means:
- It CANNOT be NULL
- It CANNOT be empty
- It MUST have a value when inserting

---

## Field Mapping Reference

| Request Field | Entity Field | Database Column | Type | Nullable | Required |
|---|---|---|---|---|---|
| kitchenName | kitchenName | kitchen_name | VARCHAR(100) | NO | ✅ |
| ownerName | ownerName | kitchen_owner_name | VARCHAR(100) | NO | ✅ |
| address | address | kitchen_address | VARCHAR(255) | NO | ✅ |
| ownerContact | ownerContact | kitchen_owner_contact | VARCHAR(15) | NO | ✅ |
| ownerEmail | ownerEmail | kitchen_owner_email | VARCHAR(100) | NO | ✅ |
| description | description | kitchen_description | TEXT | YES | ❌ |
| city | city | kitchen_city | VARCHAR(50) | YES | ❌ |
| state | state | kitchen_state | VARCHAR(50) | YES | ❌ |
| postalCode | postalCode | kitchen_postal_code | VARCHAR(10) | YES | ❌ |
| country | country | kitchen_country | VARCHAR(50) | YES | ❌ |
| latitude | latitude | latitude | DECIMAL | YES | ❌ |
| longitude | longitude | longitude | DECIMAL | YES | ❌ |
| deliveryArea | deliveryArea | delivery_area | VARCHAR(255) | YES | ❌ |
| cuisineTypes | cuisineTypes | cuisine_types | VARCHAR(500) | YES | ❌ |
| alternateContact | alternateContact | kitchen_alternate_contact | VARCHAR(15) | YES | ❌ |
| alternateEmail | alternateEmail | kitchen_alternate_email | VARCHAR(100) | YES | ❌ |

---

## ✅ FIX SUMMARY

**Problem**: Missing `address` field in request  
**Solution**: Include `address` in request JSON with valid value (5-255 characters)  
**Status**: ✅ RESOLVED

---

## Next Steps

1. Update your request to include the `address` field
2. Rebuild the project: `mvn clean package -DskipTests`
3. Test the endpoint with the corrected request
4. Verify kitchen is created successfully

---

**Last Updated**: February 4, 2026  
**Status**: ✅ READY FOR TESTING
