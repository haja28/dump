# URGENT: Cart Routing Fix - Action Required

## Current Status
✅ **Configuration Fixed**: The API Gateway source configuration has been updated with the cart route
❌ **NOT DEPLOYED YET**: The API Gateway is still running with the old configuration

## Why the Error Still Occurs
The error `No static resource api/v1/orders/cart/items` is still happening because:

1. **The API Gateway has NOT been restarted** with the new configuration
2. **The compiled configuration** in `api-gateway/target/classes/application.yml` is still using the OLD configuration
3. **Requests to `/api/v1/orders/cart/**`** are still being routed to the order-service's OrderController (not CartController)

## REQUIRED ACTIONS (Do This Now!)

### Step 1: Stop the API Gateway
In the terminal window running the API Gateway:
```
Press Ctrl+C to stop it
```

### Step 2: Rebuild the API Gateway
Run ONE of these commands:

**Option A - Using the script:**
```powershell
cd C:\workspace\makanforyou
.\restart-api-gateway.bat
```

**Option B - Manual commands:**
```powershell
cd C:\workspace\makanforyou
mvn clean install -pl api-gateway -am -DskipTests
```

### Step 3: Restart the API Gateway
```powershell
cd C:\workspace\makanforyou\api-gateway
mvn spring-boot:run
```

**OR** restart all services:
```powershell
cd C:\workspace\makanforyou
.\startup.bat
```

### Step 4: Verify the Fix
After restart, check the API Gateway logs. You should see:
```
Mapped [GET] /api/v1/cart
Mapped [POST] /api/v1/cart/items
```

## What Was Changed

### Before (WRONG):
```
Request: POST http://localhost:8080/api/v1/orders/cart/items
         ↓
API Gateway routes to: http://localhost:8084/api/v1/orders/cart/items
         ↓
OrderController tries to match "cart" as {orderId}
         ↓
ERROR: Cannot convert "cart" to Long
```

### After (CORRECT):
```
Request: POST http://localhost:8080/api/v1/cart/items
         ↓
API Gateway routes to: http://localhost:8084/api/v1/cart/items
         ↓
CartController handles the request
         ↓
SUCCESS: Cart item added
```

## Important Notes

### ⚠️ Client Code Must Also Be Updated
If your client/frontend is calling:
- ❌ `http://localhost:8080/api/v1/orders/cart/items` (WRONG)

Change it to:
- ✅ `http://localhost:8080/api/v1/cart/items` (CORRECT)

### Direct Service Access (Bypass Gateway)
If you want to test the order-service directly without the gateway:
```bash
# This works NOW (no gateway needed)
curl -X GET http://localhost:8084/api/v1/cart -H "X-User-Id: 1"

curl -X POST http://localhost:8084/api/v1/cart/items \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"menuItemId": 1, "quantity": 2}'
```

## Files Modified
1. ✅ `api-gateway/src/main/resources/application.yml` - Cart route added
2. ⚠️ `api-gateway/target/classes/application.yml` - Needs rebuild to update
3. ✅ `CART_ROUTING_FIX.md` - Documentation created
4. ✅ `restart-api-gateway.bat` - Helper script created

## Verification Checklist
- [ ] API Gateway stopped
- [ ] API Gateway rebuilt (`mvn clean install`)
- [ ] API Gateway restarted
- [ ] Check logs for cart route mappings
- [ ] Test: `curl -X GET http://localhost:8080/api/v1/cart -H "X-User-Id: 1"`
- [ ] Test: `curl -X POST http://localhost:8080/api/v1/cart/items -H "X-User-Id: 1" -H "Content-Type: application/json" -d '{"menuItemId": 1, "quantity": 2}'`
- [ ] Update client code to use `/api/v1/cart/**` (if needed)

## Need Help?
See: `CART_ROUTING_FIX.md` for detailed troubleshooting steps
