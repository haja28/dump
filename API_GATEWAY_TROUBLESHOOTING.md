# API Gateway Troubleshooting Guide

## Problem: Still Getting 404 Errors

### Step 1: Verify API Gateway is Running
```bash
curl http://localhost:8080/health
```

**Expected response:**
```json
{"status":"UP","service":"API Gateway"}
```

**If you get connection refused:** API Gateway is not running
- Start it: `cd api-gateway && mvn spring-boot:run`

**If you get a different 404:** Continue to Step 2

---

## Problem: Backend Service Not Found (502/503)

This happens when you try to access backend services that aren't running.

### Check Which Services Are Running

**Test Auth Service:**
```bash
curl http://localhost:8081/health
```

**Test Kitchen Service:**
```bash
curl http://localhost:8082/health
```

**Test Menu Service:**
```bash
curl http://localhost:8083/health
```

**Test Order Service:**
```bash
curl http://localhost:8084/health
```

### Service Not Running?

Start the missing service:
```bash
cd path/to/service && mvn spring-boot:run
```

---

## Problem: Port Already in Use

### Find What's Using Port 8080
```bash
netstat -ano | findstr :8080
```

### Kill Process (Windows)
```bash
taskkill /PID <process_id> /F
```

Or just change port in `application.yml`:
```yaml
server:
  port: 8081  # Changed from 8080
```

---

## Problem: Database Connection Error

### Check MySQL is Running
```bash
mysql -u root -p -e "SELECT 1"
```

### Check Database Exists
```bash
mysql -u root -p -e "SHOW DATABASES;"
```

### Create Database (if needed)
```bash
mysql -u root -p < database_schema.sql
```

### Update Database Credentials

Edit `application.yml` in each service:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db_name
    username: your_username
    password: your_password
```

---

## Problem: Java Version Error

### Check Java Version
```bash
java -version
```

Required: **Java 17 or higher**

### Install Java 17+
- Download from https://www.oracle.com/java/technologies/downloads/
- Set JAVA_HOME environment variable

---

## Problem: Maven Build Fails

### Clean Build
```bash
mvn clean install -DskipTests
```

### Clear Maven Cache
```bash
rmdir /s /q %USERPROFILE%\.m2\repository
mvn clean install -DskipTests
```

### Check Maven Version
```bash
mvn -version
```

Required: **Maven 3.6.0 or higher**

---

## Problem: Compilation Errors

### Common Issues

**Missing imports:**
```bash
cd service && mvn clean compile
```

**Lombok not working:**
- Ensure IDE has Lombok plugin installed
- Run: `mvn clean compile`

**Duplicate classes:**
- Check for duplicate entity definitions
- Run: `mvn clean install`

---

## Problem: CORS Errors

If frontend can't call the gateway, check CORS configuration:

**Add to api-gateway/application.yml:**
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

---

## Problem: JWT Authentication Errors

### Check JWT Token
1. Make sure token is in Authorization header: `Authorization: Bearer <token>`
2. Token should be obtained from `/api/v1/auth/login`
3. Token validity period (default: 24 hours)

### Debug JWT Issues
Enable debug logging:
```yaml
logging:
  level:
    com.makanforyou: DEBUG
    org.springframework.security: DEBUG
```

---

## Problem: No Response from Endpoint

### Verify Endpoint Path
- Path should start with `/api/v1/`
- Check spelling and case sensitivity
- Available paths:
  - `/api/v1/auth/**`
  - `/api/v1/kitchens/**`
  - `/api/v1/menu-items/**`
  - `/api/v1/menu-labels/**`
  - `/api/v1/orders/**`

### Test with Postman
1. Create new request
2. Method: GET (or POST)
3. URL: `http://localhost:8080/api/v1/endpoint`
4. Add headers if needed:
   - `Authorization: Bearer <token>`
   - `Content-Type: application/json`

---

## Debugging Tips

### Enable Debug Logging
Add to any service's `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.makanforyou: DEBUG
    org.springframework: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

### Check Service Logs
Look for:
- Database connection errors
- Missing beans
- Configuration issues
- Port conflicts

### Test Individual Services
Start only one service and test it directly:
```bash
curl http://localhost:8081/health  # Auth Service
```

### Use Chrome DevTools
1. Open Developer Tools (F12)
2. Go to Network tab
3. Try the request
4. Check response headers and body

---

## Getting Help

Check these files for more information:
- `API_GATEWAY_404_FIX.md` - Complete setup guide
- `API_GATEWAY_FIX_SUMMARY.md` - What was changed
- `API_GATEWAY_QUICK_REFERENCE.md` - Quick commands
- Service logs - Check detailed error messages

## Contact

If problems persist:
1. Check all service logs for error messages
2. Verify all prerequisites are installed
3. Ensure all services are running on correct ports
4. Check database connection and schema
