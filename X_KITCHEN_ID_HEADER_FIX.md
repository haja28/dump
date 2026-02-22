# X-Kitchen-Id Header Forwarding Fix

## Problem
The API Gateway was not forwarding the `X-Kitchen-Id` header to downstream services (order-service, cart-service, etc.). 

**Symptoms:**
- Flutter app correctly sends `X-Kitchen-Id: 1` header
- API Gateway (port 8080) receives the header
- But when forwarding to order-service (port 8084), the header is lost
- Results in 400 Bad Request errors with "Missing required request header 'X-Kitchen-Id'"

## Root Cause
Spring Cloud Gateway by default does not preserve all custom headers when forwarding requests to downstream services. The `PreserveHostHeader` filter was missing from:
1. Default filters (global configuration)
2. Individual route configurations for cart-service and order-service

## Solution Applied

### Changes to `api-gateway/src/main/resources/application.yml`

#### 1. Added `PreserveHostHeader` to Default Filters
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: Retry
          args:
            retries: 3
            series: SERVER_ERROR
        - PreserveHostHeader  # ← ADDED
        - name: RemoveRequestHeader
          args:
            name: X-Forwarded-Prefix
```

#### 2. Updated Cart Service Route
```yaml
# Cart Service (Order Service)
- id: cart-service
  uri: http://localhost:8084
  predicates:
    - Path=/api/v1/cart,/api/v1/cart/**
  filters:
    - PreserveHostHeader  # ← ADDED
```

#### 3. Updated Order Service Route
```yaml
# Order Service
- id: order-service
  uri: http://localhost:8084
  predicates:
    - Path=/api/v1/orders/**
  filters:
    - PreserveHostHeader  # ← ADDED
    - RewritePath=/api/v1/orders/(?<segment>.*), /api/v1/orders/${segment}
```

## How It Works

The `PreserveHostHeader` filter ensures that:
- Custom headers (like `X-Kitchen-Id`) are preserved when forwarding
- The original `Host` header is maintained
- All request headers are passed through to downstream services

## CORS Configuration (Already Correct)
The CORS configuration already exposes the `X-Kitchen-Id` header:
```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedHeaders:
        - "*"
      exposedHeaders:
        - X-Kitchen-Id  # ✓ Already configured
        - Authorization
```

## Testing the Fix

### 1. Restart API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```

### 2. Test with cURL
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "X-Kitchen-Id: 1" \
  -d '{
    "deliveryAddress": "123 Test St",
    "deliveryInstructions": "Ring doorbell"
  }'
```

### 3. Verify in Logs
Check that the API Gateway forwards the header:
```
DEBUG c.m.gateway - Forwarding request to order-service with headers: {X-Kitchen-Id=[1], ...}
```

### 4. Test with Flutter App
The Flutter app should now work correctly:
```dart
final response = await http.post(
  Uri.parse('http://localhost:8080/api/v1/orders'),
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer $token',
    'X-Kitchen-Id': kitchenId.toString(),  // ✓ Will be forwarded
  },
  body: jsonEncode(orderData),
);
```

## Files Modified
- ✅ `api-gateway/src/main/resources/application.yml`

## Impact
- **Scope**: All requests through API Gateway to downstream services
- **Breaking Changes**: None
- **Backward Compatibility**: ✓ Fully compatible
- **Services Affected**: 
  - order-service (primary fix)
  - cart-service (primary fix)
  - All other services benefit from header preservation

## Notes
- The Flutter code was already correct - no changes needed on the frontend
- This was purely a backend API Gateway configuration issue
- The fix ensures all custom headers are preserved, not just `X-Kitchen-Id`
- The `RemoveRequestHeader` filter for `X-Forwarded-Prefix` is still applied to avoid confusion with proxy headers

## Related Documentation
- [API_GATEWAY_QUICK_REFERENCE.md](API_GATEWAY_QUICK_REFERENCE.md)
- [ORDER_CART_MANAGEMENT_API.md](ORDER_CART_MANAGEMENT_API.md)
- Spring Cloud Gateway Docs: https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-preservehostheader-gatewayfilter-factory

