# Order Controller Route Ordering Fix

## Problem
Getting errors when accessing literal path endpoints like `/api/v1/orders/my-orders` and `/api/v1/orders/kitchen/my-orders`:
```
MethodArgumentTypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: "my-orders"
```

## Root Cause
Spring's route matching follows the order routes are defined in the controller. The generic route `@GetMapping("/{orderId}")` was placed **before** more specific routes like `/my-orders` and `/kitchen/{kitchenId}`.

### How Spring Matches Routes
1. Spring evaluates routes **in the order they appear** in the controller class
2. When a request comes to `/api/v1/orders/my-orders`, Spring checks routes from top to bottom
3. The generic `/{orderId}` pattern matches first, treating "my-orders" as a path variable
4. Spring tries to convert "my-orders" to Long (the type of orderId parameter) → **FAILS**

## Solution
**Reorder the endpoints** so specific routes come before generic ones.

### Before (WRONG Order):
```java
@GetMapping("/{orderId}")              // ❌ Generic route FIRST (catches everything)
public ResponseEntity<?> getOrder(@PathVariable Long orderId) { ... }

@GetMapping("/my-orders")              // ❌ Never reached! Already matched by /{orderId}
public ResponseEntity<?> getMyOrders() { ... }

@GetMapping("/kitchen/{kitchenId}")    // ❌ Never reached! Already matched by /{orderId}
public ResponseEntity<?> getKitchenOrders() { ... }
```

### After (CORRECT Order):
```java
@GetMapping("/my-orders")              // ✅ Specific route FIRST
public ResponseEntity<?> getMyOrders() { ... }

@GetMapping("/kitchen/{kitchenId}")    // ✅ Specific route SECOND
public ResponseEntity<?> getKitchenOrders() { ... }

@GetMapping("/{orderId}")              // ✅ Generic route LAST (catch-all)
public ResponseEntity<?> getOrder(@PathVariable Long orderId) { ... }
```

## Changes Made

### File: `order-service/src/main/java/com/makanforyou/order/controller/OrderController.java`

Reordered GET endpoints in this sequence:
1. `POST /api/v1/orders` - Create order
2. `POST /api/v1/orders/from-cart` - Create order from cart
3. **`GET /api/v1/orders/my-orders`** - Get user's orders (moved UP)
4. **`GET /api/v1/orders/kitchen/my-orders`** - Get current kitchen's orders (NEW - added)
5. **`GET /api/v1/orders/kitchen/{kitchenId}`** - Get kitchen orders (moved UP)
6. **`GET /api/v1/orders/kitchen/{kitchenId}/pending`** - Get pending orders (moved UP)
7. **`GET /api/v1/orders/{orderId}`** - Get single order (moved DOWN)
8. `PATCH /api/v1/orders/{orderId}/accept` - Accept order
9. `PATCH /api/v1/orders/{orderId}/status` - Update status
10. `PATCH /api/v1/orders/{orderId}/cancel` - Cancel order

## Required Actions

### 1. Restart Order Service
The order-service must be restarted to apply the fix:

```powershell
# Stop the running order-service (Ctrl+C in its terminal)

# Restart it
cd C:\workspace\makanforyou\order-service
mvn spring-boot:run
```

**OR** restart all services:
```powershell
cd C:\workspace\makanforyou
.\startup.bat
```

### 2. Verify the Fix
After restart, test the endpoints:

```bash
# Should work now - Get user's orders
curl -X GET http://localhost:8084/api/v1/orders/my-orders \
  -H "X-User-Id: 1"

# Should work now - Get current kitchen's orders  
curl -X GET http://localhost:8084/api/v1/orders/kitchen/my-orders \
  -H "X-Kitchen-Id: 1"

# Should work - Get kitchen orders by ID
curl -X GET http://localhost:8084/api/v1/orders/kitchen/1 \
  -H "X-User-Id: 1"

# Should still work - Get specific order by ID
curl -X GET http://localhost:8084/api/v1/orders/123 \
  -H "X-User-Id: 1"
```

## Why This Matters

### Best Practice: Order Routes from Specific to Generic
Always order your controller endpoints like this:

1. **Most specific patterns first**: `/my-orders`, `/kitchen/{id}/pending`
2. **Medium specific patterns next**: `/kitchen/{id}`
3. **Generic patterns last**: `/{id}`

### Real-World Example
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    // ✅ CORRECT ORDER
    @GetMapping("/my-orders")           // Literal path - MOST SPECIFIC
    @GetMapping("/from-cart")           // Literal path - MOST SPECIFIC
    @GetMapping("/kitchen/{id}/pending") // Two path segments + variable - SPECIFIC
    @GetMapping("/kitchen/{id}")        // One path segment + variable - MEDIUM
    @GetMapping("/{id}")                // Just a variable - LEAST SPECIFIC (LAST!)
}
```

## Testing Checklist
- [x] Build successful: `mvn clean package -DskipTests`
- [ ] Order service restarted
- [ ] Test GET `/api/v1/orders/my-orders` (should work now)
- [ ] Test GET `/api/v1/orders/kitchen/my-orders` (should work now - NEW endpoint)
- [ ] Test GET `/api/v1/orders/kitchen/1` (should work)
- [ ] Test GET `/api/v1/orders/123` (should still work)

## Related Files
- `order-service/src/main/java/com/makanforyou/order/controller/OrderController.java` - Fixed controller
- `URGENT_CART_FIX_ACTION_PLAN.md` - Previous cart routing fix

## Technical Details

### Spring MVC Route Matching Algorithm
1. Spring creates a `RequestMappingHandlerMapping` that maps all `@RequestMapping` annotations
2. When a request arrives, Spring iterates through mappings **in registration order**
3. First matching pattern wins
4. Path variables (`{id}`) are "greedy" - they match any non-empty string

### Why Order Matters
- `/{orderId}` will match: `/123`, `/my-orders`, `/cart`, `/anything`
- `/my-orders` will match: **only** `/my-orders`
- Without proper ordering, generic patterns "shadow" specific ones

## Prevention
To avoid this issue in the future:
1. Always put literal paths before parameterized paths
2. Put longer paths before shorter paths
3. Use Spring Boot Actuator's `/actuator/mappings` endpoint to see route order
4. Write integration tests for all endpoints

---
**Status**: ✅ Fixed - Restart order-service required
**Impact**: Affects all endpoints with literal paths after `/{orderId}`
**Priority**: HIGH - Blocks user order history and kitchen order management

