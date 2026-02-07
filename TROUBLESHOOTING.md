# Troubleshooting Guide - Makan For You

## Common Issues and Solutions

### Authentication Issues

#### Issue: "Missing or invalid Authorization header"
**Cause**: Missing JWT token in Authorization header

**Solution**:
1. Ensure you're sending the Authorization header
2. Format should be: `Authorization: Bearer {token}`
3. Token must not be expired

**Example**:
```bash
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:8080/api/v1/orders/my-orders
```

---

#### Issue: "Invalid or expired token"
**Cause**: JWT token has expired or is invalid

**Solution**:
1. Re-login to get a new token
2. Use refresh token endpoint to get new access token:
```bash
POST /api/v1/auth/refresh
{
  "refreshToken": "your_refresh_token"
}
```

---

#### Issue: JWT validation error at gateway
**Cause**: JWT secret mismatch between gateway and auth service

**Solution**:
1. Ensure all services use the same JWT secret
2. Restart all services after changing JWT secret
3. Check `application.yml` in each service:
```yaml
jwt:
  secret: same_secret_in_all_services
```

---

### Database Connection Issues

#### Issue: "Communications link failure with the MySQL server"
**Cause**: MySQL server is not running or connection URL is incorrect

**Solution**:
1. Check if MySQL is running:
```bash
# Windows
services.msc (search for MySQL)

# Linux
sudo systemctl status mysql

# macOS
mysql.server status
```

2. Start MySQL if not running:
```bash
# Windows
net start MySQL80

# Linux
sudo systemctl start mysql

# macOS
mysql.server start
```

3. Verify connection URL in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_auth_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root
```

4. Test MySQL connection directly:
```bash
mysql -h localhost -u root -p -e "SELECT 1;"
```

---

#### Issue: "Access denied for user 'root'@'localhost'"
**Cause**: Incorrect database credentials

**Solution**:
1. Verify your MySQL username and password
2. Update credentials in `application.yml`
3. Reset MySQL root password if forgotten:
```bash
# Stop MySQL first
sudo systemctl stop mysql

# Start without password
sudo mysqld_safe --skip-grant-tables &

# Connect and reset
mysql -u root

# Inside MySQL:
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
```

---

#### Issue: "No database selected"
**Cause**: Database doesn't exist or name is incorrect

**Solution**:
1. Create the required databases:
```sql
CREATE DATABASE makan_auth_db;
CREATE DATABASE makan_kitchen_db;
CREATE DATABASE makan_menu_db;
CREATE DATABASE makan_order_db;
```

2. Verify database exists:
```bash
mysql -u root -p -e "SHOW DATABASES;"
```

---

### Redis Issues

#### Issue: "Could not get a resource from the pool"
**Cause**: Redis server is not running or not accessible

**Solution**:
1. Check if Redis is running:
```bash
# Linux
sudo systemctl status redis-server

# macOS
redis-cli ping

# Windows (if installed)
redis-server.exe
```

2. Start Redis if not running:
```bash
# Linux
sudo systemctl start redis-server

# macOS with Homebrew
brew services start redis

# Docker
docker run -d -p 6379:6379 redis:latest
```

3. Test Redis connection:
```bash
redis-cli ping
# Should return: PONG
```

4. Verify Redis configuration in `application.yml`:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 60000
```

---

### Service Port Issues

#### Issue: "Address already in use"
**Cause**: Port is already occupied by another application

**Solution**:
1. Find process using the port:
```bash
# Linux/macOS
lsof -i :8080
lsof -i :8081
lsof -i :8082
lsof -i :8083
lsof -i :8084

# Windows
netstat -ano | findstr :8080
```

2. Kill the process:
```bash
# Linux/macOS
kill -9 <PID>

# Windows
taskkill /PID <PID> /F
```

3. Or configure different port in `application.yml`:
```yaml
server:
  port: 9080
```

---

#### Issue: "Connection refused" when calling other services
**Cause**: Target service is not running or address is incorrect

**Solution**:
1. Verify all services are running
2. Check service URLs in gateway configuration
3. Test service connectivity:
```bash
curl http://localhost:8081/swagger-ui.html
curl http://localhost:8082/swagger-ui.html
curl http://localhost:8083/swagger-ui.html
curl http://localhost:8084/swagger-ui.html
```

---

### API Request Issues

#### Issue: "404 Not Found"
**Cause**: Wrong endpoint path or service is not running

**Solution**:
1. Verify endpoint path (case-sensitive):
   - Correct: `/api/v1/kitchens`
   - Incorrect: `/api/v1/Kitchens` or `/api/v1/kitchen`

2. Check if service is running:
```bash
curl http://localhost:8080/api/v1/kitchens
```

3. Check Swagger documentation for correct endpoint

---

#### Issue: "400 Bad Request"
**Cause**: Invalid request body or parameters

**Solution**:
1. Validate JSON syntax (check for missing quotes, commas)
2. Ensure required fields are present:
```json
{
  "firstName": "John",  // Required
  "lastName": "Doe",    // Required
  "email": "john@example.com",  // Required
  "phoneNumber": "9876543210",  // Required - 10-15 digits
  "password": "SecurePassword123"  // Required - min 8 chars
}
```

3. Verify field types:
   - String fields should be quoted
   - Number fields should not be quoted
   - Boolean fields should be true/false (lowercase)

4. Check Content-Type header:
```bash
curl -H "Content-Type: application/json" http://localhost:8080/...
```

---

#### Issue: "409 Conflict - Email already exists"
**Cause**: Email or phone number already registered

**Solution**:
1. Use a different email/phone number for registration
2. Or login with existing credentials if user already exists

---

### Search and Filter Issues

#### Issue: "Empty results on search"
**Cause**: No matching items or filters too restrictive

**Solution**:
1. Try without filters:
```bash
# Without filters
curl "http://localhost:8080/api/v1/menu-items/search?query=biryani"

# With filters
curl "http://localhost:8080/api/v1/menu-items/search?query=biryani&minPrice=5&maxPrice=50"
```

2. Check if items exist in database:
```bash
# Create a menu item first
POST /api/v1/menu-items
{
  "itemName": "Biryani",
  "cost": 12.99,
  ...
}
```

3. Verify label names match exactly (case-sensitive):
```bash
# List all labels first
curl http://localhost:8080/api/v1/menu-labels

# Then use correct label names in search
curl "http://localhost:8080/api/v1/menu-items/search?labels=halal,spicy"
```

---

### Performance Issues

#### Issue: "Slow API responses"
**Cause**: Database queries are inefficient or Redis is not caching

**Solution**:
1. Enable query logging to identify slow queries:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        show_sql: false
        format_sql: true
        generate_statistics: true
logging:
  level:
    org.hibernate.stat: DEBUG
```

2. Ensure Redis is running:
```bash
redis-cli INFO stats
```

3. Check database indexes:
```sql
-- Show indexes on a table
SHOW INDEX FROM kitchen_menu;
```

4. Optimize queries in service layer

---

### Deployment Issues

#### Issue: Docker container exits immediately
**Cause**: Application failed to start

**Solution**:
1. Check container logs:
```bash
docker logs <container_id>
```

2. Verify environment variables are set:
```bash
docker run -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/makan_auth_db ...
```

3. Ensure dependencies (MySQL, Redis) are ready:
```bash
docker-compose up --build
```

---

#### Issue: Services can't connect to each other in Docker
**Cause**: Hostname resolution issue

**Solution**:
1. Use service names as hostnames in Docker Compose:
   - Gateway to Auth Service: `http://auth-service:8081`
   - Not `http://localhost:8081`

2. Verify networks in docker-compose.yml

3. Check DNS resolution:
```bash
docker exec <container> nslookup auth-service
```

---

### Build Issues

#### Issue: "Maven build fails"
**Cause**: Dependencies not found or compilation errors

**Solution**:
1. Update Maven cache:
```bash
mvn clean install -U
```

2. Check Java version (should be 17+):
```bash
java -version
```

3. Check for compilation errors:
```bash
mvn compile
```

4. Clear Maven cache if corrupted:
```bash
rm -rf ~/.m2/repository
mvn clean install
```

---

## Debug Mode

### Enable Debug Logging
Add to `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.makanforyou: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Enable SQL Logging
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### Test Endpoints with Postman/cURL

**Register user:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "9876543210",
    "password": "SecurePassword123",
    "role": "CUSTOMER"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePassword123"
  }'
```

**Get current user:**
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "X-User-Id: 1"
```

## Getting Help

1. Check Spring Boot logs: `logs/` directory
2. Review Swagger documentation: http://localhost:8080/swagger-ui.html
3. Check database directly with MySQL client
4. Enable debug logging for more detailed information
5. Review GitHub issues or community forums for similar problems

## Contact Support

For additional help:
- Email: support@makanforyou.com
- Documentation: https://github.com/makanforyou/docs
- Issues: https://github.com/makanforyou/backend/issues
