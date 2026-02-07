# API Gateway & Services - Complete Startup Guide

## Overview

Based on your API Gateway debug logs, the routing is working correctly! The logs show that requests are being properly mapped to the `kitchen-service` route. However, to get a successful response, all backend services need to be running.

## Quick Start (3 Steps)

### Step 1: Run the Startup Script
**Windows:**
```bash
double-click startup.bat
```

Or from PowerShell:
```bash
.\startup.bat
```

This will:
- ✓ Check Java and Maven are installed
- ✓ Build all services (mvn clean install)
- ✓ Start all 5 services in separate windows

**Linux/Mac:**
```bash
chmod +x startup.sh
./startup.sh
```

### Step 2: Wait for All Services to Start
Each window will show:
```
2026-01-31T12:xx:xx...  INFO ... Started [ServiceName]Application
```

Wait 30-60 seconds for all services to initialize.

### Step 3: Run Diagnostics
```bash
powershell -ExecutionPolicy Bypass -File diagnose.ps1
```

Should show all services as **RUNNING** and **RESPONDING**.

---

## Manual Startup (Alternative)

Open 5 separate PowerShell/CMD windows and run in each:

**Window 1 - Auth Service (8081):**
```bash
cd auth-service
mvn spring-boot:run
```

**Window 2 - Kitchen Service (8082):**
```bash
cd kitchen-service
mvn spring-boot:run
```

**Window 3 - Menu Service (8083):**
```bash
cd menu-service
mvn spring-boot:run
```

**Window 4 - Order Service (8084):**
```bash
cd order-service
mvn spring-boot:run
```

**Window 5 - API Gateway (8080):**
```bash
cd api-gateway
mvn spring-boot:run
```

Wait for each to show "Started [ServiceName]Application" before starting the next one.

---

## Verify Everything Works

### Quick Test
```bash
curl http://localhost:8080/api/v1/kitchens
```

**Expected Response (200 OK):**
```json
[]
```

### Health Checks
```bash
# Gateway Health
curl http://localhost:8080/health

# Individual Services
curl http://localhost:8081/health  # Auth
curl http://localhost:8082/health  # Kitchen
curl http://localhost:8083/health  # Menu
curl http://localhost:8084/health  # Order
```

**Expected Response for each:**
```json
{"status":"UP","service":"[ServiceName]"}
```

---

## What's Happening Under the Hood

### API Gateway Request Flow

When you request `http://localhost:8080/api/v1/kitchens`:

1. **Route Matching** ✓ 
   - Path `/api/v1/kitchens/**` matches `kitchen-service` route

2. **Filters Applied** ✓
   - Retry filter (up to 3 retries on server errors)
   - RewritePath filter (rewrites path)
   - JWT Authentication filter

3. **Request Forwarded** ✓
   - Sends to `http://localhost:8082/api/v1/kitchens`

4. **Response Returned**
   - Kitchen Service responds with list of kitchens

---

## Troubleshooting

### Problem: "Connection refused" when running services

**Cause:** Port is already in use

**Solution:**
```bash
# Find what's using port 8082 (example)
netstat -ano | findstr :8082

# Kill the process
taskkill /PID <process_id> /F
```

### Problem: Services won't start

**Check Java:**
```bash
java -version
# Should show Java 17 or higher
```

**Check Maven:**
```bash
mvn -version
# Should show Maven 3.8 or higher
```

**Clean and rebuild:**
```bash
mvn clean install -DskipTests
```

### Problem: Database errors in logs

**Solution:** Create databases manually:
```sql
CREATE DATABASE makan_auth_db CHARACTER SET utf8mb4;
CREATE DATABASE makan_kitchen_db CHARACTER SET utf8mb4;
CREATE DATABASE makan_menu_db CHARACTER SET utf8mb4;
CREATE DATABASE makan_order_db CHARACTER SET utf8mb4;
```

### Problem: Still getting 404 errors

**Step 1:** Check Kitchen Service directly
```bash
curl http://localhost:8082/api/v1/kitchens
```

If this returns 404 → Kitchen Service has issues (check logs)

If this returns 200 → Gateway routing issue (check debug logs)

---

## Useful Scripts & Tools

### Diagnostic Script
```bash
powershell -ExecutionPolicy Bypass -File diagnose.ps1
```

Checks:
- ✓ Which services are running
- ✓ Health endpoint responses
- ✓ Kitchen endpoint accessibility
- ✓ MySQL status
- ✓ Recommendations

### Startup Script
```bash
.\startup.bat
```

Automatically:
- ✓ Checks Java & Maven
- ✓ Builds project
- ✓ Starts all 5 services in separate windows

### Startup Script (Linux/Mac)
```bash
./startup.sh
```

Same functionality for Unix-like systems.

---

## Debug Logs Interpretation

Your debug logs show:

```
Route matched: kitchen-service
Mapping [Exchange: GET http://localhost:8080/api/v1/kitchens] to Route{...}
```

✅ **This is GOOD** - it means:
- Gateway recognized the request
- Matched it to the kitchen-service route
- Applied all filters correctly

The 404 you're getting means:
- Kitchen Service is not responding
- Or Kitchen Service doesn't have the `/api/v1/kitchens` endpoint

**Next Step:** Verify Kitchen Service is running and responsive

---

## Available Endpoints (When All Services Running)

### Health Checks
- `GET /health` - API Gateway health
- `GET /api/v1/health` - API Gateway with version

### Auth Service (/api/v1/auth/**)
- `POST /api/v1/auth/register` - Register user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/validate` - Validate token

### Kitchen Service (/api/v1/kitchens/**)
- `GET /api/v1/kitchens` - List kitchens
- `POST /api/v1/kitchens` - Register kitchen
- `GET /api/v1/kitchens/{id}` - Get kitchen
- `PUT /api/v1/kitchens/{id}` - Update kitchen

### Menu Service (/api/v1/menu-items/**, /api/v1/menu-labels/**)
- `GET /api/v1/menu-items` - List items
- `POST /api/v1/menu-items` - Add item
- `GET /api/v1/menu-labels` - List labels
- `POST /api/v1/menu-labels` - Add label

### Order Service (/api/v1/orders/**)
- `GET /api/v1/orders` - List orders
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders/{id}` - Get order

---

## Complete Documentation

For detailed information, see:

1. **[SETUP_AND_DEPLOYMENT.md](SETUP_AND_DEPLOYMENT.md)**
   - Complete setup guide
   - Docker deployment
   - Kubernetes deployment
   - Production settings

2. **[API_GATEWAY_404_FIX.md](API_GATEWAY_404_FIX.md)**
   - How 404 errors are now handled
   - Available endpoints
   - Backend service requirements

3. **[API_GATEWAY_FIX_SUMMARY.md](API_GATEWAY_FIX_SUMMARY.md)**
   - What was changed in the code
   - Implementation details
   - Testing procedures

4. **[API_GATEWAY_DIAGNOSTICS.md](API_GATEWAY_DIAGNOSTICS.md)**
   - In-depth troubleshooting
   - Step-by-step diagnostics
   - Debug log analysis

5. **[API_GATEWAY_QUICK_REFERENCE.md](API_GATEWAY_QUICK_REFERENCE.md)**
   - Quick commands
   - Common operations
   - Fast reference

6. **[API_GATEWAY_TROUBLESHOOTING.md](API_GATEWAY_TROUBLESHOOTING.md)**
   - Common issues and solutions
   - Debugging tips
   - Port conflicts
   - Database issues

---

## Next Steps

1. ✅ Run `startup.bat` to start all services
2. ✅ Wait 1 minute for all services to initialize
3. ✅ Run `diagnose.ps1` to verify all services are healthy
4. ✅ Test `curl http://localhost:8080/api/v1/kitchens`
5. ✅ Start building your application!

---

## Performance Tips

- **First request is slower** (cold start) - this is normal
- **Subsequent requests are faster** (warm cache)
- Gateway logs show request timing in debug mode
- Monitor CPU/memory if running many services

---

## Getting Help

If you encounter issues:

1. Check the appropriate guide from the list above
2. Run `diagnose.ps1` to get detailed status
3. Check service console logs for errors
4. Verify all prerequisites are installed
5. Try clean build: `mvn clean install -DskipTests`

---

## Summary

Your API Gateway is configured correctly! The debug logs prove it:
- ✅ Routes are matched
- ✅ Filters are applied
- ✅ Requests are forwarded
- ✅ JWT Authentication is ready

Just ensure all 5 backend services are running on their correct ports (8081-8084) and you'll have a fully functional microservices system!
