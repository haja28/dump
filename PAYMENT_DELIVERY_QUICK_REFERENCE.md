# Payment and Delivery Services - Quick Reference Guide

## Services Overview

| Aspect | Payment Service | Delivery Service |
|--------|-----------------|------------------|
| **Port** | 8085 | 8086 |
| **Database** | makan_payment_db | makan_delivery_db |
| **Primary Table** | payments | deliveries |
| **API Endpoints** | 8 | 12 |
| **Status Enum** | PaymentStatus | DeliveryStatus |
| **Real-time Tracking** | ❌ | ✅ (WebSocket) |

---

## Database Dependencies

### Payment Service

```
payments (PRIMARY TABLE)
    ├── order_id → orders.order_id (UNIQUE)
    └── user_id → users.user_id
```

### Delivery Service

```
deliveries (PRIMARY TABLE)
    ├── order_id → orders.order_id (UNIQUE)
    ├── kitchen_id → kitchens.kitchen_id
    ├── user_id → users.user_id
    └── item_id → kitchen_menu.item_id
```

---

## Payment API Quick Reference

### Base URL
```
http://localhost:8085/api/v1/payments
```

### Endpoints Summary

| # | Method | Endpoint | Purpose | Returns |
|---|--------|----------|---------|---------|
| 1 | POST | / | Create payment | 201 |
| 2 | GET | /{id} | Get by payment ID | 200 |
| 3 | GET | /order/{orderId} | Get by order ID | 200 |
| 4 | PUT | /{id}/process | Process payment | 200 |
| 5 | PUT | /{id}/refund | Refund payment | 200 |
| 6 | GET | /user/{userId} | List user payments | 200 |
| 7 | GET | /stats/user/{userId} | Payment stats | 200 |
| 8 | PATCH | /{id}/status | Update status | 200 |

### Quick Examples

**Create Payment:**
```bash
curl -X POST http://localhost:8085/api/v1/payments \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 5,
    "paymentAmount": 599.99,
    "paymentMethod": "CREDIT_CARD"
  }'
```

**Process Payment:**
```bash
curl -X PUT http://localhost:8085/api/v1/payments/101/process \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-2025-001234",
    "paymentMethod": "CREDIT_CARD"
  }'
```

**Refund Payment:**
```bash
curl -X PUT http://localhost:8085/api/v1/payments/101/refund \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refundAmount": 599.99,
    "refundReason": "Order cancelled"
  }'
```

### Payment Status Transitions

```
PENDING
  ├─→ COMPLETED (process payment)
  └─→ FAILED (payment declined)

COMPLETED
  └─→ REFUNDED (refund initiated)

FAILED
  └─→ PENDING (retry)
```

### Payment Methods Supported

- CREDIT_CARD
- DEBIT_CARD
- NET_BANKING
- WALLET
- CASH_ON_DELIVERY (COD)
- UPI

---

## Delivery API Quick Reference

### Base URL
```
http://localhost:8086/api/v1/deliveries
```

### Endpoints Summary

| # | Method | Endpoint | Purpose | Returns |
|---|--------|----------|---------|---------|
| 1 | POST | / | Create delivery | 201 |
| 2 | GET | /{id} | Get by delivery ID | 200 |
| 3 | GET | /order/{orderId} | Get by order ID | 200 |
| 4 | PUT | /{id}/assign | Assign partner | 200 |
| 5 | PUT | /{id}/pickup | Mark pickup | 200 |
| 6 | PUT | /{id}/in-transit | Mark in-transit | 200 |
| 7 | PUT | /{id}/complete | Mark complete | 200 |
| 8 | PUT | /{id}/failed | Mark failed | 200 |
| 9 | GET | /kitchen/{kitchenId} | Kitchen deliveries | 200 |
| 10 | GET | /user/{userId} | User deliveries | 200 |
| 11 | GET | /partner/{name}/stats | Partner stats | 200 |
| 12 | GET | /stats | Overall stats | 200 |

### Quick Examples

**Create Delivery:**
```bash
curl -X POST http://localhost:8086/api/v1/deliveries \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "kitchenId": 2,
    "userId": 5,
    "itemId": 10,
    "estimatedDeliveryTime": "2026-02-03T12:00:00Z",
    "deliveryNotes": "Leave at gate"
  }'
```

**Assign Delivery Partner:**
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/assign \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "partnerName": "Rajesh Kumar",
    "partnerPhone": "9876543210"
  }'
```

**Mark Pickup:**
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/pickup \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pickupTime": "2026-02-03T10:50:00Z",
    "notes": "Order picked up successfully"
  }'
```

**Update In-Transit:**
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/in-transit \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentLocation": "12.9716° N, 77.5946° E",
    "notes": "On the way"
  }'
```

**Complete Delivery:**
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/complete \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryTime": "2026-02-03T11:45:00Z",
    "notes": "Delivered successfully",
    "recipientName": "Mr. Smith"
  }'
```

### Delivery Status Transitions

```
PENDING
  ├─→ ASSIGNED (partner assigned)
  └─→ FAILED (assignment failed)

ASSIGNED
  ├─→ PICKED_UP (order picked from kitchen)
  └─→ FAILED (pickup failed)

PICKED_UP
  ├─→ IN_TRANSIT (on the way)
  └─→ FAILED (transit issue)

IN_TRANSIT
  ├─→ DELIVERED (successfully delivered)
  └─→ FAILED (delivery failed)

FAILED
  └─→ ASSIGNED (retry)
```

---

## Common HTTP Status Codes

| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST |
| 400 | Bad Request | Invalid input/validation error |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists/invalid state |
| 500 | Server Error | Internal server error |

---

## Error Response Format

```json
{
  "timestamp": "2026-02-03T11:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid payment amount",
  "path": "/api/v1/payments",
  "traceId": "uuid-here"
}
```

---

## Database Indexes

### Payment Service Indexes
- `idx_order_id` - O(1) lookup by order
- `idx_user_id` - User payment history
- `idx_payment_status` - Filter by status
- `idx_payment_date` - Date range queries

### Delivery Service Indexes
- `idx_order_id` - O(1) lookup by order
- `idx_kitchen_id` - Kitchen deliveries
- `idx_user_id` - User delivery history
- `idx_delivery_status` - Filter by status
- `idx_delivery_time` - Date range queries

---

## Pagination Parameters

All list endpoints support:

```
?page=0          # Page number (0-indexed, default: 0)
&size=20         # Items per page (default: 20, max: 100)
&sortBy=field    # Sort field (varies by endpoint)
&sortOrder=DESC  # ASC or DESC (default: DESC)
```

Example:
```
GET /api/v1/payments/user/5?page=0&size=10&sortOrder=DESC
```

---

## JWT Authentication

### Token Format
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Claims
```json
{
  "sub": "user_id",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CUSTOMER|KITCHEN|ADMIN",
  "iat": 1675000000,
  "exp": 1675086400
}
```

### Token Expiration
- Default: 24 hours
- Refresh token: 7 days

---

## Role-Based Access Control (RBAC)

| Endpoint | CUSTOMER | KITCHEN | ADMIN |
|----------|----------|---------|-------|
| POST /payments | ✅ | ❌ | ✅ |
| GET /payments/{id} | ✅ | ❌ | ✅ |
| PUT /payments/{id}/refund | ✅ | ❌ | ✅ |
| GET /deliveries/user/{id} | ✅ | ❌ | ✅ |
| GET /deliveries/kitchen/{id} | ❌ | ✅ | ✅ |
| PUT /deliveries/{id}/assign | ❌ | ✅ | ✅ |
| GET /deliveries/stats | ❌ | ❌ | ✅ |

---

## Configuration Keys

### Payment Service (application.yml)

```yaml
# Database
spring.datasource.url: jdbc:mysql://localhost:3306/makan_payment_db
spring.datasource.username: root
spring.datasource.password: root

# Server
server.port: 8085

# JWT
jwt.secret: your-256-bit-key
jwt.expiration: 86400000

# App
app.payment.processing-timeout: 300
app.payment.max-refund-percentage: 100
```

### Delivery Service (application.yml)

```yaml
# Database
spring.datasource.url: jdbc:mysql://localhost:3306/makan_delivery_db
spring.datasource.username: root
spring.datasource.password: root

# Server
server.port: 8086

# JWT
jwt.secret: your-256-bit-key
jwt.expiration: 86400000

# App
app.delivery.default-estimated-time-minutes: 60
app.delivery.late-delivery-threshold: 30
app.delivery.max-delivery-distance: 50
```

---

## Health Check URLs

```
# Payment Service
http://localhost:8085/actuator/health

# Delivery Service
http://localhost:8086/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

## Metrics Collection

### Available Metrics Endpoints

```
# Payment Service Metrics
http://localhost:8085/actuator/metrics

# Delivery Service Metrics
http://localhost:8086/actuator/metrics
```

### Key Metrics

- `http.server.requests` - HTTP request counts
- `db.connection.pool` - Database connection info
- `system.cpu.usage` - CPU utilization
- `process.uptime` - Service uptime

---

## Troubleshooting Quick Fixes

### Service Won't Start

1. Check port availability:
   ```bash
   lsof -i :8085  # macOS/Linux
   netstat -ano | findstr :8085  # Windows
   ```

2. Check database connection:
   ```bash
   mysql -u root -p
   SHOW DATABASES;
   USE makan_payment_db;
   SHOW TABLES;
   ```

3. Check logs:
   ```bash
   tail -f logs/payment-service.log
   ```

### Payment Creation Fails

1. Verify order exists:
   ```sql
   SELECT * FROM orders WHERE order_id = 1;
   ```

2. Verify user exists:
   ```sql
   SELECT * FROM users WHERE user_id = 5;
   ```

3. Check for duplicate payment:
   ```sql
   SELECT * FROM payments WHERE order_id = 1;
   ```

### Delivery Creation Fails

1. Verify all references:
   ```sql
   SELECT * FROM orders WHERE order_id = 1;
   SELECT * FROM users WHERE user_id = 5;
   SELECT * FROM kitchens WHERE kitchen_id = 2;
   SELECT * FROM kitchen_menu WHERE item_id = 10;
   ```

2. Check for duplicate delivery:
   ```sql
   SELECT * FROM deliveries WHERE order_id = 1;
   ```

---

## Performance Tips

### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### Caching
```yaml
spring:
  cache:
    type: simple
```

### Batch Processing
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
```

---

## Testing with Postman

### Import Collection
1. Copy endpoints from documentation
2. Create collection in Postman
3. Set environment variable: `base_url` = `http://localhost:8085` or `8086`
4. Set environment variable: `token` = `YOUR_JWT_TOKEN`

### Use in Requests
```
URL: {{base_url}}/api/v1/payments
Headers: Authorization: Bearer {{token}}
```

---

## File Locations

```
payment-service/
├── pom.xml
├── src/main/java/com/makanforyou/payment/
│   ├── entity/Payment.java
│   ├── dto/PaymentDTO.java
│   └── controller/PaymentController.java
└── src/main/resources/application.yml

delivery-service/
├── pom.xml
├── src/main/java/com/makanforyou/delivery/
│   ├── entity/Delivery.java
│   ├── dto/DeliveryDTO.java
│   └── controller/DeliveryController.java
└── src/main/resources/application.yml

database_schema.sql  # Create tables
PAYMENT_AND_DELIVERY_APIS.md  # Full API docs
PAYMENT_DELIVERY_INTEGRATION_GUIDE.md  # Setup guide
```

---

## Next Implementation Steps

- [ ] Implement PaymentService business logic
- [ ] Implement DeliveryService business logic
- [ ] Create PaymentRepository interface
- [ ] Create DeliveryRepository interface
- [ ] Add input validation (Bean Validation)
- [ ] Setup exception handling
- [ ] Configure Swagger/OpenAPI
- [ ] Add unit tests
- [ ] Add integration tests
- [ ] Setup CI/CD pipeline
- [ ] Configure monitoring/alerting
- [ ] Performance testing

---

## Support Resources

- **API Documentation**: `PAYMENT_AND_DELIVERY_APIS.md`
- **Integration Guide**: `PAYMENT_DELIVERY_INTEGRATION_GUIDE.md`
- **Implementation Summary**: `PAYMENT_DELIVERY_IMPLEMENTATION_SUMMARY.md`
- **Database Schema**: `database_schema.sql`

---

## Quick Command Reference

```bash
# Build services
mvn clean package -DskipTests

# Run Payment Service
java -jar payment-service/target/payment-service-1.0.0.jar

# Run Delivery Service
java -jar delivery-service/target/delivery-service-1.0.0.jar

# Test Payment API
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8085/api/v1/payments/1

# Test Delivery API
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8086/api/v1/deliveries/1

# Check Health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
```

---

**Last Updated**: February 3, 2026  
**Version**: 1.0  
**Status**: Ready for Development
