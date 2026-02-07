# Quick Fix Reference - API Gateway 404 Error

## Changes Made

### 1. Added Health Check Endpoints
- `GET /health` - Simple health check
- `GET /api/v1/health` - API version health check

### 2. Added Global Error Handler
- Provides helpful error messages instead of blank 404 page
- Shows available endpoints in error responses

### 3. Updated Gateway Configuration
- Added default retry filter
- Added root path route (redirects / to /health)
- Better error handling configuration

## Quick Test

### Start API Gateway
```bash
cd C:\workspace\makanforyou\api-gateway
mvn spring-boot:run
```

### Test Health
```bash
curl http://localhost:8080/health
```

Expected output:
```json
{"status":"UP","service":"API Gateway"}
```

## Full Setup (Start All Services)

**Terminal 1 - Auth Service (8081):**
```bash
cd C:\workspace\makanforyou\auth-service && mvn spring-boot:run
```

**Terminal 2 - Kitchen Service (8082):**
```bash
cd C:\workspace\makanforyou\kitchen-service && mvn spring-boot:run
```

**Terminal 3 - Menu Service (8083):**
```bash
cd C:\workspace\makanforyou\menu-service && mvn spring-boot:run
```

**Terminal 4 - Order Service (8084):**
```bash
cd C:\workspace\makanforyou\order-service && mvn spring-boot:run
```

**Terminal 5 - API Gateway (8080):**
```bash
cd C:\workspace\makanforyou\api-gateway && mvn spring-boot:run
```

## Available Endpoints

- `/health` - Gateway health
- `/api/v1/health` - API health
- `/api/v1/auth/**` - Auth endpoints
- `/api/v1/kitchens/**` - Kitchen endpoints
- `/api/v1/menu-items/**` - Menu item endpoints
- `/api/v1/menu-labels/**` - Menu label endpoints
- `/api/v1/orders/**` - Order endpoints

## Problem Solved

✅ 404 errors now show helpful information
✅ Health checks available for monitoring
✅ Root path redirects to health check
✅ Better error messages for invalid endpoints
