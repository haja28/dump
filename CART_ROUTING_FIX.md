# Cart Routing Fix

## Problem
The cart endpoints were returning routing errors:
- `MethodArgumentTypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: "cart"`
- `NoResourceFoundException: No static resource api/v1/orders/cart/items`

## Root Cause
The API Gateway was routing all requests to `/api/v1/orders/**` to the order-service, including requests to `/api/v1/orders/cart/**`. 

However, the CartController is mounted at `/api/v1/cart`, NOT `/api/v1/orders/cart`. This caused:
1. Requests to `/api/v1/orders/cart` were being matched by the OrderController's `@GetMapping("/{orderId}")` endpoint
2. Spring tried to convert "cart" to a Long for the `orderId` parameter, causing the type mismatch error

## Solution
Added a dedicated cart route in the API Gateway configuration **before** the orders route:

```yaml
# Cart Service (Order Service)
- id: cart-service
  uri: http://localhost:8084
  predicates:
    - Path=/api/v1/cart,/api/v1/cart/**

# Order Service
- id: order-service
  uri: http://localhost:8084
  predicates:
    - Path=/api/v1/orders/**
  filters:
    - RewritePath=/api/v1/orders/(?<segment>.*), /api/v1/orders/${segment}
```

## Key Points
1. **Route Order Matters**: The cart route must come before the orders route in the gateway configuration
2. **No Rewrite Needed**: Cart requests don't need path rewriting since the CartController already expects `/api/v1/cart/**`
3. **Path Patterns**: The cart route matches both `/api/v1/cart` and `/api/v1/cart/**` to handle all cart endpoints

## Correct API Endpoints

### Cart Endpoints (via API Gateway)
```
GET    http://localhost:8080/api/v1/cart              - Get cart
POST   http://localhost:8080/api/v1/cart/items        - Add item to cart
PUT    http://localhost:8080/api/v1/cart/items/{id}   - Update cart item
DELETE http://localhost:8080/api/v1/cart/items/{id}   - Remove cart item
DELETE http://localhost:8080/api/v1/cart              - Clear cart
POST   http://localhost:8080/api/v1/cart/coupon       - Apply coupon
DELETE http://localhost:8080/api/v1/cart/coupon       - Remove coupon
POST   http://localhost:8080/api/v1/cart/refresh      - Refresh cart
POST   http://localhost:8080/api/v1/cart/validate     - Validate cart
```

### Order Endpoints (via API Gateway)
```
GET    http://localhost:8080/api/v1/orders            - List orders
POST   http://localhost:8080/api/v1/orders            - Create order
POST   http://localhost:8080/api/v1/orders/from-cart  - Create order from cart
GET    http://localhost:8080/api/v1/orders/{orderId}  - Get order details
PUT    http://localhost:8080/api/v1/orders/{orderId}/status - Update order status
DELETE http://localhost:8080/api/v1/orders/{orderId}  - Cancel order
```

## Testing
After restarting the API Gateway, test with:

```bash
# Get cart (requires X-User-Id header)
curl -X GET http://localhost:8080/api/v1/cart -H "X-User-Id: 1"

# Add item to cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "quantity": 2}'
```

## Files Modified
- `api-gateway/src/main/resources/application.yml` - Added cart route configuration

## Deployment Steps

### 1. Rebuild and Restart API Gateway
The API Gateway **MUST** be rebuilt and restarted for the changes to take effect:

```powershell
# Stop the API Gateway if it's running (Ctrl+C in its terminal)

# Navigate to the project root
cd C:\workspace\makanforyou

# Rebuild the API Gateway
mvn clean install -pl api-gateway -am

# Start the API Gateway
cd api-gateway
mvn spring-boot:run
```

### 2. Verify the Fix
Once the API Gateway is restarted, verify it's using the new configuration:

```powershell
# The logs should show the cart route is registered
# Look for: "Mapped [POST] /api/v1/cart/items" type messages
```

## Troubleshooting

### Still Getting "No static resource api/v1/orders/cart/items" Error?

This error means:
1. **API Gateway not restarted**: The old configuration is still active in memory
2. **Client using wrong URL**: The client/app is calling `/api/v1/orders/cart` instead of `/api/v1/cart`
3. **Compiled files not updated**: The `target/classes/application.yml` has the old configuration

**Solution**: 
- Stop the API Gateway
- Run `mvn clean install` to ensure fresh build
- Restart the API Gateway
- Update client code to use `/api/v1/cart/**` endpoints (NOT `/api/v1/orders/cart/**`)

### Direct Service Access
If testing directly (bypassing API Gateway):
```bash
# Access order-service directly on port 8084
curl -X GET http://localhost:8084/api/v1/cart -H "X-User-Id: 1"
curl -X POST http://localhost:8084/api/v1/cart/items -H "X-User-Id: 1" -H "Content-Type: application/json" -d '{"menuItemId": 1, "quantity": 2}'
```

