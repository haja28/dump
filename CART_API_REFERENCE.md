# Cart API Quick Reference

## Correct Cart Endpoints (via API Gateway - port 8080)

### Get Current User's Cart
```bash
curl -X GET http://localhost:8080/api/v1/cart \
  -H "X-User-Id: 1"
```

### Add Item to Cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "menuItemId": 1,
    "quantity": 2,
    "specialInstructions": "No onions please"
  }'
```

### Update Cart Item Quantity
```bash
curl -X PUT http://localhost:8080/api/v1/cart/items/1 \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3,
    "specialInstructions": "Extra spicy"
  }'
```

### Remove Item from Cart
```bash
curl -X DELETE http://localhost:8080/api/v1/cart/items/1 \
  -H "X-User-Id: 1"
```

### Clear Entire Cart
```bash
curl -X DELETE http://localhost:8080/api/v1/cart \
  -H "X-User-Id: 1"
```

### Apply Coupon to Cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/coupon \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "SAVE10"
  }'
```

### Remove Coupon from Cart
```bash
curl -X DELETE http://localhost:8080/api/v1/cart/coupon \
  -H "X-User-Id: 1"
```

### Refresh Cart (Update Prices)
```bash
curl -X POST http://localhost:8080/api/v1/cart/refresh \
  -H "X-User-Id: 1"
```

### Validate Cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/validate \
  -H "X-User-Id: 1"
```

## Direct Service Access (Bypass Gateway - port 8084)

Same endpoints but use port 8084:
```bash
# Example: Get cart directly from order-service
curl -X GET http://localhost:8084/api/v1/cart -H "X-User-Id: 1"
```

## ❌ WRONG URLs (Do NOT Use These)
```
http://localhost:8080/api/v1/orders/cart           ← WRONG!
http://localhost:8080/api/v1/orders/cart/items     ← WRONG!
```

## ✅ CORRECT URLs
```
http://localhost:8080/api/v1/cart                   ← CORRECT
http://localhost:8080/api/v1/cart/items            ← CORRECT
```

## Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "id": 1,
        "menuItemId": 1,
        "menuItemName": "Nasi Lemak",
        "kitchenId": 1,
        "kitchenName": "Mama's Kitchen",
        "quantity": 2,
        "unitPrice": 8.50,
        "subtotal": 17.00,
        "specialInstructions": "No onions please"
      }
    ],
    "subtotal": 17.00,
    "couponCode": null,
    "couponDiscount": 0.00,
    "total": 17.00,
    "itemCount": 2,
    "expiresAt": "2026-02-10T23:05:00.000+08:00"
  },
  "message": "Cart retrieved successfully",
  "timestamp": "2026-02-09T23:10:00.000+08:00"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Cart item not found",
  "timestamp": "2026-02-09T23:10:00.000+08:00"
}
```

## Required Headers
- `X-User-Id`: The ID of the user (required for all cart operations)
- `Content-Type: application/json` (for POST/PUT requests)

## Notes
1. Cart expires after 24 hours of inactivity
2. Prices are automatically refreshed when retrieving cart
3. Items from different kitchens are kept in the same cart
4. When creating an order, you can use `POST /api/v1/orders/from-cart` to convert cart to order
