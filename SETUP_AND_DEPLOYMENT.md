# Makan For You - Setup and Deployment Guide

## Quick Start Guide

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+ (optional but recommended)
- Git

### Step 1: Clone the Project
```bash
git clone https://github.com/your-org/makanforyou.git
cd makanforyou
```

### Step 2: Database Setup

Create the required databases:

```sql
CREATE DATABASE makan_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE makan_kitchen_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE makan_menu_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE makan_order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Create database user (optional):
```sql
CREATE USER 'makan_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON makan_auth_db.* TO 'makan_user'@'localhost';
GRANT ALL PRIVILEGES ON makan_kitchen_db.* TO 'makan_user'@'localhost';
GRANT ALL PRIVILEGES ON makan_menu_db.* TO 'makan_user'@'localhost';
GRANT ALL PRIVILEGES ON makan_order_db.* TO 'makan_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 3: Configure Services

Update the following files with your database credentials:

**auth-service/src/main/resources/application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_auth_db?useSSL=false&serverTimezone=UTC
    username: makan_user
    password: secure_password
```

Repeat for other services with their respective databases.

### Step 4: Generate JWT Secret

Generate a secure JWT secret (at least 256 bits):
```bash
# Using OpenSSL
openssl rand -base64 32

# Or use online generator: https://www.random.org/
```

Update all services' `application.yml`:
```yaml
jwt:
  secret: your_generated_secret_key_here
```

### Step 5: Build and Run

**Build all modules:**
```bash
mvn clean install
```

**Run services in order (in separate terminals):**

Terminal 1 - Auth Service:
```bash
cd auth-service
mvn spring-boot:run
```

Terminal 2 - Kitchen Service:
```bash
cd kitchen-service
mvn spring-boot:run
```

Terminal 3 - Menu Service:
```bash
cd menu-service
mvn spring-boot:run
```

Terminal 4 - Order Service:
```bash
cd order-service
mvn spring-boot:run
```

Terminal 5 - API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
```

### Step 6: Verify Services

Check if all services are running:
```bash
curl http://localhost:8080/api/v1/kitchens
```

You should get a 200 response with empty kitchens list.

## Quick Diagnostics

### Run Diagnostic Script (Windows)
```bash
powershell -ExecutionPolicy Bypass -File diagnose.ps1
```

This script will check:
- ✓ Which services are running
- ✓ Which services are responding to health checks
- ✓ If Kitchen Service endpoint is accessible
- ✓ MySQL service status
- ✓ Provide recommendations for missing services

### Manual Health Checks

**API Gateway:**
```bash
curl http://localhost:8080/health
```

**Kitchen Service:**
```bash
curl http://localhost:8082/health
```

**All Services:**
```bash
curl http://localhost:8081/health  # Auth
curl http://localhost:8082/health  # Kitchen
curl http://localhost:8083/health  # Menu
curl http://localhost:8084/health  # Order
```

### Test Individual Endpoints

**Kitchen Service directly:**
```bash
curl http://localhost:8082/api/v1/kitchens
```

**Through API Gateway:**
```bash
curl http://localhost:8080/api/v1/kitchens
```

## Detailed Guides

- **[API Gateway 404 Fix](API_GATEWAY_404_FIX.md)** - How to resolve 404 errors
- **[API Gateway Diagnostics](API_GATEWAY_DIAGNOSTICS.md)** - In-depth troubleshooting
- **[API Gateway Quick Reference](API_GATEWAY_QUICK_REFERENCE.md)** - Quick commands

## Docker Deployment

### Docker Compose Setup

Create a `docker-compose.yml` in the root directory:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  auth-service:
    build:
      context: .
      dockerfile: auth-service/Dockerfile
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_auth_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      JWT_SECRET: your_jwt_secret_here
    depends_on:
      mysql:
        condition: service_healthy

  kitchen-service:
    build:
      context: .
      dockerfile: kitchen-service/Dockerfile
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_kitchen_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      mysql:
        condition: service_healthy

  menu-service:
    build:
      context: .
      dockerfile: menu-service/Dockerfile
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_menu_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_REDIS_HOST: redis
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy

  order-service:
    build:
      context: .
      dockerfile: order-service/Dockerfile
    ports:
      - "8084:8084"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_order_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      mysql:
        condition: service_healthy

  api-gateway:
    build:
      context: .
      dockerfile: api-gateway/Dockerfile
    ports:
      - "8080:8080"
    environment:
      JWT_SECRET: your_jwt_secret_here
    depends_on:
      - auth-service
      - kitchen-service
      - menu-service
      - order-service

volumes:
  mysql_data:
```

Create Dockerfile for each service. Example for auth-service:

**auth-service/Dockerfile:**
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn -pl auth-service -DskipTests clean package

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Run with Docker Compose:
```bash
docker-compose up -d
```

## Production Deployment

### Environment Variables

Create a `.env` file for production settings:

```env
# Database
DB_HOST=your-db-host
DB_PORT=3306
DB_USER=makan_prod_user
DB_PASSWORD=secure_db_password

# JWT
JWT_SECRET=your_production_jwt_secret
JWT_EXPIRATION=900000
REFRESH_TOKEN_EXPIRATION=604800000

# Redis
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Kubernetes Deployment

Create deployment manifests for each service. Example:

**auth-service-deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  labels:
    app: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: your-registry/makan/auth-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: makan-config
              key: db-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: makan-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 10
          periodSeconds: 5
```

Deploy to Kubernetes:
```bash
kubectl apply -f auth-service-deployment.yaml
kubectl apply -f kitchen-service-deployment.yaml
kubectl apply -f menu-service-deployment.yaml
kubectl apply -f order-service-deployment.yaml
kubectl apply -f api-gateway-deployment.yaml
```

## Monitoring and Logging

### Spring Boot Actuator

Add to each service's `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Enable endpoints in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

Access metrics:
```bash
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/health
```

### Logging Configuration

Create `logback-spring.xml` in each service's `src/main/resources/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss} - %msg%n"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <logger name="com.makanforyou" level="DEBUG"/>
</configuration>
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Issues
```bash
# Test MySQL connection
mysql -h localhost -u root -p makan_auth_db

# Check if MySQL is running
sudo systemctl status mysql

# Start MySQL if not running
sudo systemctl start mysql
```

### Redis Connection Issues
```bash
# Test Redis connection
redis-cli ping

# Check if Redis is running
sudo systemctl status redis-server

# Start Redis if not running
sudo systemctl start redis-server
```

### JWT Token Validation Fails
- Ensure JWT secret is the same across all services
- Check token expiration time
- Verify Authorization header format: `Bearer {token}`

## Performance Optimization

### Database Optimization
- Create indexes on frequently queried columns
- Enable query caching in MySQL
- Use connection pooling (HikariCP)

### Caching Strategy
- Cache menu items for 1 hour
- Cache kitchen data for 30 minutes
- Cache labels for 12 hours
- Invalidate cache on updates

### API Response Compression
Add to `application.yml`:
```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024
```

## Backup and Recovery

### Database Backup
```bash
# Create backup
mysqldump -u root -p makan_auth_db > backup_auth.sql
mysqldump -u root -p makan_kitchen_db > backup_kitchen.sql
mysqldump -u root -p makan_menu_db > backup_menu.sql
mysqldump -u root -p makan_order_db > backup_order.sql

# Restore backup
mysql -u root -p makan_auth_db < backup_auth.sql
```

### Scheduled Backups
Create a cron job:
```bash
0 2 * * * /home/user/backup_databases.sh
```

**backup_databases.sh:**
```bash
#!/bin/bash
BACKUP_DIR="/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)

mysqldump -u root -p$DB_PASSWORD makan_auth_db > $BACKUP_DIR/auth_$DATE.sql
mysqldump -u root -p$DB_PASSWORD makan_kitchen_db > $BACKUP_DIR/kitchen_$DATE.sql
mysqldump -u root -p$DB_PASSWORD makan_menu_db > $BACKUP_DIR/menu_$DATE.sql
mysqldump -u root -p$DB_PASSWORD makan_order_db > $BACKUP_DIR/order_$DATE.sql

# Keep only last 30 days
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

## Support and Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Cloud Gateway: https://spring.io/projects/spring-cloud-gateway
- JWT.io: https://jwt.io
- MySQL Documentation: https://dev.mysql.com/doc/
- Redis Documentation: https://redis.io/documentation
