# REQUEST PARAMETER FIXES - QUICK TEST GUIDE

**Status:** ✅ All Parameter Naming Issues Fixed  
**Date:** February 2, 2026

---

## 🎯 WHAT WAS FIXED

All `@RequestParam` annotations in 4 controllers now have explicit `name` attributes. This fixes Spring MVC parameter resolution errors.

**Controllers Fixed:**
1. ✅ MenuItemController - 14 parameters fixed
2. ✅ OrderController - 6 parameters fixed  
3. ✅ MenuLabelController - 4 parameters fixed
4. ✅ KitchenController - 8 parameters fixed

**Total: 32 parameters fixed**

---

## 🧪 QUICK TEST COMMANDS

### Build Services
```bash
cd C:\workspace\makanforyou
mvn clean install -DskipTests
```

### Start All Services (5 terminals)
```bash
# Terminal 1 - Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 2 - Kitchen Service
cd kitchen-service && mvn spring-boot:run

# Terminal 3 - Menu Service  
cd menu-service && mvn spring-boot:run

# Terminal 4 - Order Service
cd order-service && mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway && mvn spring-boot:run
```

---

## ✅ TEST ENDPOINTS

### 1. Test Menu Search (Was Broken)
```bash
curl "http://localhost:8080/api/v1/menu-items/search?query=biryani"
```
**Expected:** 200 OK (may be empty if no menu items, but no error)

### 2. Test Kitchen List with Pagination
```bash
curl "http://localhost:8080/api/v1/kitchens?page=0&size=10"
```
**Expected:** 200 OK with paginated results

### 3. Test Kitchen Search
```bash
curl "http://localhost:8080/api/v1/kitchens/search?query=indian"
```
**Expected:** 200 OK (may be empty if no results)

### 4. Test Menu Label Creation
```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=spicy&description=Spicy%20food"
```
**Expected:** 201 CREATED

### 5. Test Order List Pagination
```bash
curl -H "X-User-Id: 1" "http://localhost:8080/api/v1/orders/my-orders?page=0&size=10"
```
**Expected:** 200 OK

---

## ❌ SIGNS OF SUCCESS

You should NO LONGER see:
- `IllegalArgumentException: Name for argument of type [String] not specified`
- `IllegalArgumentException: Name for argument of type [boolean] not specified`
- Parameter resolution errors in logs
- `parameter name information not found in class file`

---

## ✨ EXPECTED RESULTS

### Before Fixes:
```
ERROR: Internal server error
java.lang.IllegalArgumentException: Name for argument of type [java.lang.String] not specified
```

### After Fixes:
```
✅ GET /api/v1/menu-items/search?query=biryani - 200 OK
✅ GET /api/v1/kitchens?page=0&size=10 - 200 OK
✅ POST /api/v1/menu-labels?name=spicy - 201 CREATED
```

---

## 📋 WHAT CHANGED

### Example Fix
```java
// BEFORE (Error):
@GetMapping("/search")
public ResponseEntity<ApiResponse<PagedResponse<MenuItemDTO>>> searchMenuItems(
    @RequestParam(required = false) String query,  // ❌ No name!
    @RequestParam(defaultValue = "0") int page     // ❌ No name!
)

// AFTER (Fixed):
@GetMapping("/search")
public ResponseEntity<ApiResponse<PagedResponse<MenuItemDTO>>> searchMenuItems(
    @RequestParam(name = "query", required = false) String query,     // ✅ Explicit name
    @RequestParam(name = "page", defaultValue = "0") int page         // ✅ Explicit name
)
```

---

## 🎯 FILES FIXED

1. `menu-service/src/main/java/.../menu/controller/MenuItemController.java` - 14 params
2. `order-service/src/main/java/.../order/controller/OrderController.java` - 6 params
3. `menu-service/src/main/java/.../menu/controller/MenuLabelController.java` - 4 params
4. `kitchen-service/src/main/java/.../kitchen/controller/KitchenController.java` - 8 params

---

## 📊 AFFECTED ENDPOINTS

### MenuItemController
- `GET /api/v1/menu-items/search` ✅ (query parameter fixed)
- `GET /api/v1/menu-items/kitchen/{kitchenId}` ✅ (page, size fixed)

### OrderController
- `GET /api/v1/orders/my-orders` ✅ (page, size fixed)
- `GET /api/v1/orders/kitchen/{kitchenId}` ✅ (page, size fixed)
- `GET /api/v1/orders/kitchen/{kitchenId}/pending` ✅ (page, size fixed)

### MenuLabelController
- `POST /api/v1/menu-labels` ✅ (name, description fixed)
- `PUT /api/v1/menu-labels/{labelId}` ✅ (name, description fixed)

### KitchenController
- `GET /api/v1/kitchens` ✅ (approved parameter fixed)
- `GET /api/v1/kitchens/search` ✅ (query parameter fixed)
- `GET /api/v1/kitchens/by-city/{city}` ✅ (page, size fixed)

---

## 🚀 NEXT STEPS

1. Build services: `mvn clean install -DskipTests`
2. Start all services in separate terminals
3. Run test commands above
4. Verify all endpoints return 200 OK (not error responses)
5. Check logs - should be no IllegalArgumentException

---

## 💡 WHY THIS HAPPENED

Spring MVC needs parameter names for `@RequestParam` in one of two ways:
1. **Explicit name attribute:** `@RequestParam(name = "query")`
2. **Bytecode metadata:** Compile with `-parameters` flag

We fixed it using method #1 (explicit naming) which is more explicit and reliable.

---

## ✅ VERIFICATION CHECKLIST

- [ ] Built successfully with `mvn clean install`
- [ ] All services started without errors
- [ ] Menu search endpoint works: `GET /api/v1/menu-items/search?query=test`
- [ ] Kitchen list endpoint works: `GET /api/v1/kitchens?page=0&size=10`
- [ ] Kitchen search endpoint works: `GET /api/v1/kitchens/search?query=test`
- [ ] Menu labels endpoint works: `POST /api/v1/menu-labels?name=test`
- [ ] Order list endpoint works: `GET /api/v1/orders/my-orders?page=0&size=10`
- [ ] No IllegalArgumentException in logs

---

**Ready to test! Follow the build and test commands above.** ✅

