# Payment and Delivery Services - Implementation and Integration Guide

## Overview

This document provides a comprehensive guide for implementing and integrating the Payment Service and Delivery Service into the Makan For You microservices architecture.

---

## Table of Contents

1. [Service Architecture](#service-architecture)
2. [Database Setup](#database-setup)
3. [Service Configuration](#service-configuration)
4. [Building Services](#building-services)
5. [Running Services](#running-services)
6. [Service Integration](#service-integration)
7. [API Testing](#api-testing)
8. [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)

---

## Service Architecture

### Payment Service (Port 8085)

**Responsibilities:**
- Process and manage payment transactions
- Track payment status and history
- Handle refunds and reversals
- Maintain payment records in database
- Provide payment statistics and analytics

**Dependencies:**
- MySQL Database (makan_payment_db)
- Spring Boot 3.2.0
- Spring Data JPA for ORM
- Spring Cloud (Eureka, Feign)

**Key Components:**
```
payment-service/
├── src/main/java/com/makanforyou/payment/
│   ├── controller/       # REST API Controllers
│   ├── service/          # Business Logic Services
│   ├── repository/       # JPA Repositories
│   ├── entity/           # Payment Entity
│   ├── dto/              # Data Transfer Objects
│   ├── exception/        # Custom Exceptions
│   └── config/           # Spring Configuration
├── src/main/resources/
│   └── application.yml   # Service Configuration
└── pom.xml              # Maven Dependencies
```

### Delivery Service (Port 8086)

**Responsibilities:**
- Track delivery status and progress
- Manage delivery partner assignments
- Update delivery location in real-time
- Handle delivery failures and retries
- Provide delivery statistics and analytics

**Dependencies:**
- MySQL Database (makan_delivery_db)
- Spring Boot 3.2.0
- Spring Data JPA for ORM
- Spring Cloud (Eureka, Feign)
- Spring WebSocket (for real-time tracking)

**Key Components:**
```
delivery-service/
├── src/main/java/com/makanforyou/delivery/
│   ├── controller/       # REST API Controllers
│   ├── service/          # Business Logic Services
│   ├── repository/       # JPA Repositories
│   ├── entity/           # Delivery Entity
│   ├── dto/              # Data Transfer Objects
│   ├── exception/        # Custom Exceptions
│   ├── websocket/        # WebSocket Configuration
│   └── config/           # Spring Configuration
├── src/main/resources/
│   └── application.yml   # Service Configuration
└── pom.xml              # Maven Dependencies
```

---

## Database Setup

### Create Databases

Execute the following SQL commands to create dedicated databases for each service:

```sql
-- Create Payment Service Database
CREATE DATABASE makan_payment_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create Delivery Service Database
CREATE DATABASE makan_delivery_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON makan_payment_db.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON makan_delivery_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### Database Tables

The tables are already defined in the main `database_schema.sql` file. If using separate databases, replicate the relevant tables:

**Payment Service Database (makan_payment_db):**
- `payments` - Primary table for payment records
- `orders` - Referenced table (foreign key)
- `users` - Referenced table (foreign key)

**Delivery Service Database (makan_delivery_db):**
- `deliveries` - Primary table for delivery records
- `orders` - Referenced table (foreign key)
- `users` - Referenced table (foreign key)
- `kitchens` - Referenced table (foreign key)
- `kitchen_menu` - Referenced table (foreign key)

### Alternative: Shared Database

If using a single shared database for all microservices:

```sql
CREATE DATABASE makan_fyou_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Execute database_schema.sql
USE makan_fyou_db;
-- ... execute the schema file content
```

Then update `application.yml` in both services to point to the shared database:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_fyou_db
```

---

## Service Configuration

### Payment Service Configuration

Edit `payment-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_payment_db
    username: root
    password: root
    
server:
  port: 8085
  
jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000
  
# Enable specific application properties
app:
  payment:
    processing-timeout: 300
    max-refund-percentage: 100
    allowed-methods:
      - CREDIT_CARD
      - DEBIT_CARD
      - NET_BANKING
      - WALLET
      - CASH_ON_DELIVERY
      - UPI
```

### Delivery Service Configuration

Edit `delivery-service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_delivery_db
    username: root
    password: root
    
server:
  port: 8086
  
jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000
  
# Enable specific application properties
app:
  delivery:
    default-estimated-time-minutes: 60
    late-delivery-threshold: 30
    max-delivery-distance: 50
```

---

## Building Services

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Build Commands

```bash
# Build Payment Service
cd payment-service
mvn clean package -DskipTests

# Build Delivery Service
cd ../delivery-service
mvn clean package -DskipTests

# Or build both with parent pom
cd ..
mvn clean package -DskipTests -pl payment-service,delivery-service
```

### Generated Artifacts

```
payment-service/target/payment-service-1.0.0.jar
delivery-service/target/delivery-service-1.0.0.jar
```

---

## Running Services

### Option 1: Run with Maven

```bash
# Terminal 1: Start Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 2: Start Delivery Service
cd delivery-service
mvn spring-boot:run
```

### Option 2: Run with Java

```bash
# Terminal 1: Start Payment Service
java -jar payment-service/target/payment-service-1.0.0.jar

# Terminal 2: Start Delivery Service
java -jar delivery-service/target/delivery-service-1.0.0.jar
```

### Option 3: Run with Docker

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  payment-service:
    build:
      context: ./payment-service
      dockerfile: Dockerfile
    ports:
      - "8085:8085"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_payment_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql
      
  delivery-service:
    build:
      context: ./delivery-service
      dockerfile: Dockerfile
    ports:
      - "8086:8086"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/makan_delivery_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql
      
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
    volumes:
      - ./database_schema.sql:/docker-entrypoint-initdb.d/schema.sql
```

Run with:
```bash
docker-compose up -d
```

---

## Service Integration

### Order Service Integration

The Order Service should interact with Payment and Delivery Services as follows:

#### 1. Create Payment on Order Creation

```java
@PostMapping("/api/v1/orders")
public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
    // 1. Create order
    Order order = orderService.createOrder(request);
    
    // 2. Create payment record
    PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
        .orderId(order.getOrderId())
        .userId(order.getUserId())
        .paymentAmount(order.getOrderTotal())
        .paymentMethod(request.getPaymentMethod())
        .build();
    
    paymentServiceClient.createPayment(paymentRequest);
    
    // 3. Create delivery record
    DeliveryRequestDTO deliveryRequest = DeliveryRequestDTO.builder()
        .orderId(order.getOrderId())
        .kitchenId(order.getKitchenId())
        .userId(order.getUserId())
        .itemId(request.getItemId())
        .estimatedDeliveryTime(calculateEstimatedTime())
        .build();
    
    deliveryServiceClient.createDelivery(deliveryRequest);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(order));
}
```

#### 2. Listen for Payment Status Changes

```java
@Component
public class PaymentEventListener {
    
    @Async
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        // Update order status to CONFIRMED
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
        
        // Notify kitchen
        kitchenServiceClient.notifyOrderConfirmed(event.getOrderId());
    }
    
    @Async
    public void onPaymentFailed(PaymentFailedEvent event) {
        // Update order status to CANCELLED
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
        
        // Notify user
        userServiceClient.notifyPaymentFailed(event.getUserId());
    }
}
```

#### 3. Listen for Delivery Status Changes

```java
@Component
public class DeliveryEventListener {
    
    @Async
    public void onDeliveryAssigned(DeliveryAssignedEvent event) {
        // Update order status to READY
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.READY);
    }
    
    @Async
    public void onDeliveryInTransit(DeliveryInTransitEvent event) {
        // Update order status to OUT_FOR_DELIVERY
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.OUT_FOR_DELIVERY);
    }
    
    @Async
    public void onDeliveryCompleted(DeliveryCompletedEvent event) {
        // Update order status to DELIVERED
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.DELIVERED);
        
        // Trigger review creation
        reviewServiceClient.enableReviews(event.getOrderId());
    }
}
```

### Using Feign Client

Create Feign clients in the Order Service:

```java
@FeignClient(name = "payment-service", url = "http://localhost:8085")
public interface PaymentServiceClient {
    
    @PostMapping("/api/v1/payments")
    PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO request);
    
    @GetMapping("/api/v1/payments/order/{orderId}")
    PaymentResponseDTO getPaymentByOrderId(@PathVariable Integer orderId);
    
    @PutMapping("/api/v1/payments/{paymentId}/process")
    PaymentResponseDTO processPayment(
        @PathVariable Integer paymentId,
        @RequestBody ProcessPaymentRequestDTO request);
}

@FeignClient(name = "delivery-service", url = "http://localhost:8086")
public interface DeliveryServiceClient {
    
    @PostMapping("/api/v1/deliveries")
    DeliveryResponseDTO createDelivery(@RequestBody DeliveryRequestDTO request);
    
    @GetMapping("/api/v1/deliveries/order/{orderId}")
    DeliveryResponseDTO getDeliveryByOrderId(@PathVariable Integer orderId);
    
    @PutMapping("/api/v1/deliveries/{deliveryId}/assign")
    DeliveryResponseDTO assignDeliveryPartner(
        @PathVariable Integer deliveryId,
        @RequestBody AssignDeliveryRequestDTO request);
}
```

---

## API Testing

### Postman Collection

Create a Postman collection with the following endpoints:

#### Payment Service Endpoints

1. **Create Payment**
```
POST http://localhost:8085/api/v1/payments
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "orderId": 1,
  "userId": 5,
  "paymentAmount": 599.99,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN-2025-001234"
}
```

2. **Get Payment by ID**
```
GET http://localhost:8085/api/v1/payments/101
Authorization: Bearer {JWT_TOKEN}
```

3. **Get Payment by Order ID**
```
GET http://localhost:8085/api/v1/payments/order/1
Authorization: Bearer {JWT_TOKEN}
```

4. **Process Payment**
```
PUT http://localhost:8085/api/v1/payments/101/process
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "transactionId": "TXN-2025-001234",
  "paymentMethod": "CREDIT_CARD"
}
```

5. **Refund Payment**
```
PUT http://localhost:8085/api/v1/payments/101/refund
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "refundAmount": 599.99,
  "refundReason": "Order cancelled by customer"
}
```

#### Delivery Service Endpoints

1. **Create Delivery**
```
POST http://localhost:8086/api/v1/deliveries
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "orderId": 1,
  "kitchenId": 2,
  "userId": 5,
  "itemId": 10,
  "estimatedDeliveryTime": "2026-02-03T12:00:00Z",
  "deliveryNotes": "Leave at gate if no one is home"
}
```

2. **Assign Delivery Partner**
```
PUT http://localhost:8086/api/v1/deliveries/201/assign
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "partnerName": "Rajesh Kumar",
  "partnerPhone": "9876543210"
}
```

3. **Update Pickup Status**
```
PUT http://localhost:8086/api/v1/deliveries/201/pickup
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "pickupTime": "2026-02-03T10:50:00Z",
  "notes": "Order picked up successfully"
}
```

4. **Update In-Transit Status**
```
PUT http://localhost:8086/api/v1/deliveries/201/in-transit
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "currentLocation": "12.9716° N, 77.5946° E",
  "notes": "On the way to customer"
}
```

5. **Complete Delivery**
```
PUT http://localhost:8086/api/v1/deliveries/201/complete
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "deliveryTime": "2026-02-03T11:45:00Z",
  "notes": "Delivered successfully",
  "recipientName": "Mr. Smith"
}
```

### Using cURL

```bash
# Test Payment Service
curl -X POST http://localhost:8085/api/v1/payments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 5,
    "paymentAmount": 599.99,
    "paymentMethod": "CREDIT_CARD"
  }'

# Test Delivery Service
curl -X POST http://localhost:8086/api/v1/deliveries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "kitchenId": 2,
    "userId": 5,
    "itemId": 10,
    "estimatedDeliveryTime": "2026-02-03T12:00:00Z"
  }'
```

---

## Monitoring and Troubleshooting

### Health Check Endpoints

```
# Payment Service
http://localhost:8085/actuator/health

# Delivery Service
http://localhost:8086/actuator/health
```

### Metrics Endpoints

```
# Payment Service Metrics
http://localhost:8085/actuator/metrics

# Delivery Service Metrics
http://localhost:8086/actuator/metrics
```

### Common Issues and Solutions

#### Issue: Database Connection Failed

**Solution:**
```bash
# Check MySQL is running
mysql -u root -p

# Verify database exists
SHOW DATABASES;

# Check application.yml datasource URL
```

#### Issue: Port Already in Use

**Solution:**
```bash
# Find process using port 8085
lsof -i :8085  # macOS/Linux
netstat -ano | findstr :8085  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows

# Or use different port in application.yml
server:
  port: 8087
```

#### Issue: JWT Token Validation Failed

**Solution:**
1. Ensure JWT secret matches across services
2. Check token expiration
3. Verify Authorization header format: `Bearer <TOKEN>`

#### Issue: Foreign Key Constraint Failed

**Solution:**
1. Ensure referenced records exist (order, user, kitchen)
2. Check cascading delete settings
3. Verify data types match between tables

### Log Files

```
payment-service/logs/payment-service.log
delivery-service/logs/delivery-service.log
```

### Useful MySQL Commands

```sql
-- Check tables
USE makan_payment_db;
SHOW TABLES;

-- View payment records
SELECT * FROM payments;

-- View delivery records
USE makan_delivery_db;
SELECT * FROM deliveries;

-- Check foreign key relationships
SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'payments';

-- Clear logs (if needed)
TRUNCATE TABLE payments;
TRUNCATE TABLE deliveries;
```

---

## Performance Optimization

### Database Indexes

The schema already includes optimized indexes:
- `idx_order_id` - For payment/delivery lookups by order
- `idx_user_id` - For user payment/delivery history
- `idx_payment_status` - For filtering by payment status
- `idx_delivery_status` - For filtering by delivery status
- `idx_payment_date` - For date range queries
- `idx_delivery_time` - For date range queries

### Connection Pooling

Default connection pool size: 10
Increase in `application.yml` if needed:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
```

### Caching

Implement caching for frequently accessed data:

```java
@Cacheable(value = "payment", key = "#paymentId")
public PaymentResponseDTO getPaymentById(Integer paymentId) {
    // ...
}
```

Enable caching in `application.yml`:

```yaml
spring:
  cache:
    type: simple
```

---

## Deployment Checklist

- [ ] MySQL databases created and initialized
- [ ] application.yml configured with correct credentials
- [ ] JWT secret key configured in both services
- [ ] Database migration scripts executed
- [ ] Services built successfully with `mvn clean package`
- [ ] Port 8085 (Payment) available
- [ ] Port 8086 (Delivery) available
- [ ] Services started without errors
- [ ] Health check endpoints responding
- [ ] API endpoints tested with Postman/cURL
- [ ] Logs monitored for errors
- [ ] Load testing completed
- [ ] Monitoring and alerts configured

---

## Next Steps

1. Implement Service Layer (PaymentService, DeliveryService)
2. Create Repository Interfaces (PaymentRepository, DeliveryRepository)
3. Implement Exception Handling
4. Add Request/Response Validation
5. Configure Swagger/OpenAPI documentation
6. Setup CI/CD pipeline
7. Implement event publishing (RabbitMQ/Kafka)
8. Add distributed tracing (Spring Cloud Sleuth)
9. Setup monitoring with Prometheus/Grafana
10. Performance testing and optimization

---
