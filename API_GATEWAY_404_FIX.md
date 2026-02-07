# API Gateway 404 Error Resolution Guide

## Problem
You are seeing a **404 Whitelabel Error Page** when accessing the API Gateway. This means:
- The API Gateway is running correctly on port 8080
- But you're either hitting a non-existent route or the backend services aren't running

## Solution

### 1. Test the API Gateway is Running
Visit these URLs to verify the gateway is working:
- **http://localhost:8080/** - Will redirect to /health
- **http://localhost:8080/health** - Returns gateway health status
- **http://localhost:8080/api/v1/health** - Returns gateway health with version info

Expected response:
```json
{
  "status": "UP",
  "service": "API Gateway"
}
```

### 2. Start the Backend Services
The 404 errors will occur if backend services aren't running. Start them in order:

**Terminal 1 - Auth Service (port 8081):**
```bash
cd C:\workspace\makanforyou\auth-service
mvn spring-boot:run
```

**Terminal 2 - Kitchen Service (port 8082):**
```bash
cd C:\workspace\makanforyou\kitchen-service
mvn spring-boot:run
```

**Terminal 3 - Menu Service (port 8083):**
```bash
cd C:\workspace\makanforyou\menu-service
mvn spring-boot:run
```

**Terminal 4 - Order Service (port 8084):**
```bash
cd C:\workspace\makanforyou\order-service
mvn spring-boot:run
```

**Terminal 5 - API Gateway (port 8080):**
```bash
cd C:\workspace\makanforyou\api-gateway
mvn spring-boot:run
```

### 3. Available Endpoints

Once all services are running, you can access:

#### Auth Service
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/validate` - Validate token

#### Kitchen Service
- `GET /api/v1/kitchens` - List all kitchens
- `POST /api/v1/kitchens` - Register new kitchen
- `GET /api/v1/kitchens/{id}` - Get kitchen details
- `PUT /api/v1/kitchens/{id}` - Update kitchen

#### Menu Service
- `GET /api/v1/menu-items` - List all menu items
- `POST /api/v1/menu-items` - Add menu item
- `GET /api/v1/menu-items/{id}` - Get menu item details
- `GET /api/v1/menu-labels` - List all labels
- `POST /api/v1/menu-labels` - Create label

#### Order Service
- `GET /api/v1/orders` - List all orders
- `POST /api/v1/orders` - Create new order
- `GET /api/v1/orders/{id}` - Get order details

### 4. Troubleshooting

**Still getting 404?**
1. Check all backend services are running on their respective ports (8081-8084)
2. Check the API Gateway logs: `mvn spring-boot:run` output
3. Verify the URL matches one of the configured routes above
4. Make sure the path starts with `/api/v1/`

**Services won't start?**
1. Run `mvn clean compile` in the service directory
2. Check if the ports are already in use: `netstat -ano | findstr :8081` (Windows)
3. Ensure Java 17+ is installed: `java -version`
4. Check MySQL is running and database exists

**Database Issues?**
1. Ensure MySQL service is running
2. Database should be created automatically on first startup
3. Check `database_schema.sql` for manual setup if needed

## What Changed

The following improvements were made to handle 404 errors better:

1. **HealthController** - Added `/health` and `/api/v1/health` endpoints for testing
2. **GlobalErrorHandler** - Improved error responses with helpful hints
3. **API Gateway Configuration** - Added root path route and default retry filters
4. **Error Handling** - Better error messages that indicate available endpoints

Now when you get a 404, the response will show available endpoints instead of a blank whitelabel error page.
