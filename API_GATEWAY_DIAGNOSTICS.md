# API Gateway - Request Flow Analysis & Diagnostics

## Current Status ✅

Based on your debug logs, the API Gateway is **working correctly**:

### ✅ What's Working
- Route predicate matching is working
- Gateway filters are loaded and sorted properly
- JWT Authentication Filter is initialized
- Request routing is configured correctly
- Retry filters are in place

### ❌ What Might Be Wrong
The request is being routed to `http://localhost:8082` but getting a 404. This means:
1. Kitchen Service is NOT running, OR
2. Kitchen Service is running but has no configured endpoints

---

## Step-by-Step Diagnostics

### Step 1: Test API Gateway Health
```bash
curl -v http://localhost:8080/health
```

**Expected Response (200 OK):**
```json
{"status":"UP","service":"API Gateway"}
```

---

### Step 2: Verify Kitchen Service is Running
```bash
curl -v http://localhost:8082/health
```

**Expected Response (200 OK):**
```json
{"status":"UP","service":"Kitchen Service"}
```

**If you get "Connection refused":**
- Kitchen Service is NOT running
- Start it: `cd kitchen-service && mvn spring-boot:run`

---

### Step 3: Check All Services Status

**Auth Service (8081):**
```bash
curl -v http://localhost:8081/health
```

**Kitchen Service (8082):**
```bash
curl -v http://localhost:8082/health
```

**Menu Service (8083):**
```bash
curl -v http://localhost:8083/health
```

**Order Service (8084):**
```bash
curl -v http://localhost:8084/health
```

---

### Step 4: Test Kitchen Service Directly (Bypass Gateway)
```bash
curl -v http://localhost:8082/api/v1/kitchens
```

**Expected Response (200 OK):**
```json
[]
```

**If you get 404:**
- Kitchen Service might not have controllers configured
- Check if KitchenController exists and is properly annotated

---

### Step 5: Test Through Gateway
```bash
curl -v http://localhost:8080/api/v1/kitchens
```

**Expected Response (200 OK):**
```json
[]
```

**If you get 404:**
- Check Kitchen Service logs for errors
- Verify Kitchen Service database connection

---

## Common Issues & Solutions

### Issue: Kitchen Service Shows 404

**Cause:** Service doesn't have endpoints configured

**Check:**
```bash
# List all files in kitchen service
dir C:\workspace\makanforyou\kitchen-service\src\main\java\com\makanforyou\kitchen
```

**Should see:**
- `controller/` folder with `KitchenController.java`
- `entity/` folder with `Kitchen.java`
- `service/` folder with `KitchenService.java`
- `repository/` folder with `KitchenRepository.java`

**If missing:**
- Controllers need to be created
- Check `API_GATEWAY_404_FIX.md` for controller setup

---

### Issue: Database Connection Error

**Symptoms in logs:**
- "Connection refused" to MySQL
- "Unknown database"

**Fix:**
```sql
-- Create databases if not exists
CREATE DATABASE IF NOT EXISTS makan_auth_db;
CREATE DATABASE IF NOT EXISTS makan_kitchen_db;
CREATE DATABASE IF NOT EXISTS makan_menu_db;
CREATE DATABASE IF NOT EXISTS makan_order_db;
```

---

### Issue: Port Already in Use

**Windows:**
```bash
netstat -ano | findstr :8082
taskkill /PID <process_id> /F
```

**Linux/Mac:**
```bash
lsof -i :8082
kill -9 <PID>
```

---

## Complete Startup Sequence

### Terminal 1 - MySQL
```bash
# Windows - if MySQL service isn't running
net start MySQL80

# Or use MySQL Workbench to start the service
```

### Terminal 2 - Auth Service
```bash
cd C:\workspace\makanforyou\auth-service
mvn spring-boot:run
# Wait for: "Started AuthServiceApplication"
```

### Terminal 3 - Kitchen Service
```bash
cd C:\workspace\makanforyou\kitchen-service
mvn spring-boot:run
# Wait for: "Started KitchenServiceApplication"
```

### Terminal 4 - Menu Service
```bash
cd C:\workspace\makanforyou\menu-service
mvn spring-boot:run
# Wait for: "Started MenuServiceApplication"
```

### Terminal 5 - Order Service
```bash
cd C:\workspace\makanforyou\order-service
mvn spring-boot:run
# Wait for: "Started OrderServiceApplication"
```

### Terminal 6 - API Gateway
```bash
cd C:\workspace\makanforyou\api-gateway
mvn spring-boot:run
# Wait for: "Started ApiGatewayApplication"
```

---

## Detailed Testing Script

Create `test_endpoints.ps1` (PowerShell):

```powershell
Write-Host "Testing Makan For You API Gateway" -ForegroundColor Cyan

# Test Gateway Health
Write-Host "`n1. Testing API Gateway Health..." -ForegroundColor Yellow
try {
    $response = curl -s -w "`n%{http_code}" http://localhost:8080/health
    $body = $response[0..($response.Count-2)] -join "`n"
    $status = $response[-1]
    Write-Host "Status: $status" -ForegroundColor Green
    Write-Host "Body: $body" -ForegroundColor Green
} catch {
    Write-Host "ERROR: API Gateway not responding" -ForegroundColor Red
}

# Test Kitchen Service Health
Write-Host "`n2. Testing Kitchen Service Health..." -ForegroundColor Yellow
try {
    $response = curl -s -w "`n%{http_code}" http://localhost:8082/health
    $body = $response[0..($response.Count-2)] -join "`n"
    $status = $response[-1]
    Write-Host "Status: $status" -ForegroundColor Green
    Write-Host "Body: $body" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Kitchen Service not responding" -ForegroundColor Red
}

# Test Kitchen Endpoint Through Gateway
Write-Host "`n3. Testing /api/v1/kitchens through Gateway..." -ForegroundColor Yellow
try {
    $response = curl -s -w "`n%{http_code}" http://localhost:8080/api/v1/kitchens
    $body = $response[0..($response.Count-2)] -join "`n"
    $status = $response[-1]
    Write-Host "Status: $status" -ForegroundColor Green
    Write-Host "Body: $body" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Endpoint not responding" -ForegroundColor Red
}

# Test Kitchen Endpoint Directly
Write-Host "`n4. Testing /api/v1/kitchens directly on Kitchen Service..." -ForegroundColor Yellow
try {
    $response = curl -s -w "`n%{http_code}" http://localhost:8082/api/v1/kitchens
    $body = $response[0..($response.Count-2)] -join "`n"
    $status = $response[-1]
    Write-Host "Status: $status" -ForegroundColor Green
    Write-Host "Body: $body" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Direct endpoint not responding" -ForegroundColor Red
}

Write-Host "`n✅ Testing Complete!" -ForegroundColor Cyan
```

### Run the test:
```bash
powershell -ExecutionPolicy Bypass -File test_endpoints.ps1
```

---

## Log Analysis

### Where to Find Logs

**Kitchen Service Logs:**
Look for these lines in the console when running `mvn spring-boot:run`:
- ✅ "Started KitchenServiceApplication"
- ✅ "Netty started with port(s): 8082"
- ❌ Any database connection errors
- ❌ Any bean initialization errors

### Useful Debug Commands

**Enable more detailed logging:**

Edit `kitchen-service/src/main/resources/application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.makanforyou: DEBUG
    org.springframework: DEBUG
    org.springframework.web: DEBUG
    org.hibernate: DEBUG
```

**Then restart the service and look for:**
- Controller initialization
- Endpoint mapping
- Database connection
- Authentication filter setup

---

## Quick Checklist

- [ ] MySQL service is running
- [ ] All 4 databases are created
- [ ] Auth Service is running (port 8081)
- [ ] Kitchen Service is running (port 8082)
- [ ] Menu Service is running (port 8083)
- [ ] Order Service is running (port 8084)
- [ ] API Gateway is running (port 8080)
- [ ] Health endpoints respond with 200
- [ ] `/api/v1/kitchens` returns empty array `[]`

---

## What to Do Next

1. **Verify Kitchen Service is running:** `curl http://localhost:8082/health`
2. **Check Kitchen Service logs** for any startup errors
3. **Test the endpoint directly:** `curl http://localhost:8082/api/v1/kitchens`
4. **Then test through gateway:** `curl http://localhost:8080/api/v1/kitchens`
5. **If still 404:** Provide the Kitchen Service console output for analysis

---

## Debug Mode

To get even more detailed output, start Kitchen Service with debug logs:

```bash
cd kitchen-service
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

This will show:
- All request mappings
- All bean initialization
- All filter chains
- Route matching details

---

## Still Having Issues?

Provide these details:
1. Output of `curl http://localhost:8082/health`
2. Output of `curl http://localhost:8080/api/v1/kitchens`
3. Last 50 lines from Kitchen Service console
4. Last 50 lines from API Gateway console
5. MySQL connection test: `mysql -u root -p -e "SHOW DATABASES;"`
