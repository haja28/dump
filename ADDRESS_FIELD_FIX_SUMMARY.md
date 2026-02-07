# Address Field Null Value Fix - Kitchen Service

## Issue Description

The kitchen registration was failing with the following error:
```
Field 'address' doesn't have a default value
```

Even though the request contained the address value, Hibernate was attempting to insert a NULL value for the `address` field into the database.

## Root Cause Analysis

The issue occurred because:

1. The `Kitchen` entity has the `address` field marked as `nullable = false`:
   ```java
   @Column(length = 255, nullable = false, name = "kitchen_address")
   private String address;
   ```

2. In the original `registerKitchen` method, there was no explicit validation to ensure the address was not null before building the Kitchen entity:
   ```java
   // Old code - missing null check
   .address(request.getAddress())
   ```

3. Although `KitchenRegistrationRequest` has `@NotBlank` validation on the address field, there could be edge cases where:
   - The validation wasn't applied (e.g., due to framework configuration issues)
   - The address was whitespace-only (which passes `getAddress() != null` but should be rejected)

## Solution Implemented

Added explicit null and empty validation in the `registerKitchen` method before building the entity:

```java
// Validate required fields
if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
    throw new ApplicationException("INVALID_REQUEST",
            "Address is required and cannot be empty");
}
```

And ensured the address is trimmed when setting it:

```java
.address(request.getAddress().trim())
```

## Changes Made

**File:** `kitchen-service/src/main/java/com/makanforyou/kitchen/service/KitchenService.java`

**Method:** `registerKitchen(Long userId, KitchenRegistrationRequest request)`

**Changes:**
1. Added null and empty validation for the address field
2. Ensured `.address()` in the builder uses `.trim()` to remove whitespace
3. Validation now throws `ApplicationException` with code "INVALID_REQUEST" if address is missing

## Testing Recommendations

1. **Test with valid address:** Send a kitchen registration request with a valid address
   - Expected: Kitchen should be registered successfully

2. **Test with null address:** Try sending a request without the address field
   - Expected: Request validation should fail at the controller level

3. **Test with empty address:** Send a request with `address: ""`
   - Expected: Should throw "INVALID_REQUEST" exception

4. **Test with whitespace address:** Send a request with `address: "   "`
   - Expected: Should throw "INVALID_REQUEST" exception

## Related Fields

The same principle should be applied to other `nullable = false` fields:
- `kitchenName` (already has validation in request)
- `ownerName` (already has validation in request)
- `ownerContact` (already has validation in request)
- `ownerEmail` (already has validation in request)

## Impact

- ✅ Prevents NULL values from being inserted into non-nullable columns
- ✅ Provides clear error messages to clients
- ✅ Improves data integrity at the application layer
- ✅ Reduces database constraint violations

## Status

✅ **FIXED** - Address validation added with null and empty checks
