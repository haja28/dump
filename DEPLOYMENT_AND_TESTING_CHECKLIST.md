# Payment and Delivery Services - Deployment and Testing Checklist

## Pre-Deployment Checklist ✅

### Database Setup
- [ ] MySQL server running (version 8.0+)
- [ ] Database `makan_payment_db` created
- [ ] Database `makan_delivery_db` created
- [ ] Database user with proper privileges
- [ ] Tables created (from database_schema.sql)
- [ ] Indexes created
- [ ] Test data inserted (optional)

### Configuration Setup
- [ ] `payment-service/src/main/resources/application.yml` configured
  - [ ] Database URL correct
  - [ ] Database username/password correct
  - [ ] Server port 8085 available
  - [ ] JWT secret key set (change from default in production)
- [ ] `delivery-service/src/main/resources/application.yml` configured
  - [ ] Database URL correct
  - [ ] Database username/password correct
  - [ ] Server port 8086 available
  - [ ] JWT secret key set (change from default in production)

### Build Verification
- [ ] Java 17+ installed
- [ ] Maven 3.8+ installed
- [ ] `mvn clean package -DskipTests` runs successfully
- [ ] JAR files generated in target directories
- [ ] No compilation errors
- [ ] No dependency conflicts

### Code Review
- [ ] Entity classes reviewed
- [ ] DTO classes reviewed
- [ ] Controller endpoints reviewed
- [ ] Application configuration reviewed
- [ ] No hardcoded sensitive values
- [ ] Exception handling in place
- [ ] Input validation in place

---

## Service Launch Checklist ✅

### Payment Service (Port 8085)

**Pre-Launch**:
- [ ] Port 8085 is free (verify with: `lsof -i :8085` or `netstat -ano | findstr :8085`)
- [ ] Database `makan_payment_db` tables exist
- [ ] application.yml is properly configured
- [ ] JAR file exists: `payment-service/target/payment-service-1.0.0.jar`

**Launch**:
```bash
java -jar payment-service/target/payment-service-1.0.0.jar
```

**Post-Launch Verification**:
- [ ] Service starts without errors
- [ ] No exception stack traces in logs
- [ ] "Started PaymentServiceApplication" message appears
- [ ] Health endpoint responds:
  ```bash
  curl http://localhost:8085/actuator/health
  # Should return: {"status":"UP"}
  ```
- [ ] Logs show "Successfully initialized" messages

**Expected Console Output**:
```
Started PaymentServiceApplication in X.XXX seconds
Tomcat started on port(s): 8085
Hibernate initialized successfully
```

---

### Delivery Service (Port 8086)

**Pre-Launch**:
- [ ] Port 8086 is free (verify with: `lsof -i :8086` or `netstat -ano | findstr :8086`)
- [ ] Database `makan_delivery_db` tables exist
- [ ] application.yml is properly configured
- [ ] JAR file exists: `delivery-service/target/delivery-service-1.0.0.jar`

**Launch**:
```bash
java -jar delivery-service/target/delivery-service-1.0.0.jar
```

**Post-Launch Verification**:
- [ ] Service starts without errors
- [ ] No exception stack traces in logs
- [ ] "Started DeliveryServiceApplication" message appears
- [ ] Health endpoint responds:
  ```bash
  curl http://localhost:8086/actuator/health
  # Should return: {"status":"UP"}
  ```
- [ ] Logs show "Successfully initialized" messages

**Expected Console Output**:
```
Started DeliveryServiceApplication in X.XXX seconds
Tomcat started on port(s): 8086
Hibernate initialized successfully
```

---

## API Testing Checklist ✅

### Payment Service Endpoints

#### 1. Create Payment
- [ ] Endpoint: `POST /api/v1/payments`
- [ ] Expected Status: 201 Created
- [ ] Test Command:
```bash
curl -X POST http://localhost:8085/api/v1/payments \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"userId":5,"paymentAmount":599.99,"paymentMethod":"CREDIT_CARD"}'
```
- [ ] Response contains: `paymentId`, `paymentStatus: PENDING`

#### 2. Get Payment by ID
- [ ] Endpoint: `GET /api/v1/payments/{paymentId}`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8085/api/v1/payments/101
```
- [ ] Response returns payment details

#### 3. Get Payment by Order ID
- [ ] Endpoint: `GET /api/v1/payments/order/{orderId}`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8085/api/v1/payments/order/1
```
- [ ] Response returns payment for order

#### 4. Process Payment
- [ ] Endpoint: `PUT /api/v1/payments/{paymentId}/process`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8085/api/v1/payments/101/process \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"transactionId":"TXN-2025-001234","paymentMethod":"CREDIT_CARD"}'
```
- [ ] Response status: COMPLETED

#### 5. Refund Payment
- [ ] Endpoint: `PUT /api/v1/payments/{paymentId}/refund`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8085/api/v1/payments/101/refund \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refundAmount":599.99,"refundReason":"Order cancelled"}'
```
- [ ] Response status: REFUNDED

#### 6. List User Payments
- [ ] Endpoint: `GET /api/v1/payments/user/{userId}`
- [ ] Expected Status: 200 OK
- [ ] Supports pagination: `?page=0&size=10`
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  "http://localhost:8085/api/v1/payments/user/5?page=0&size=10"
```
- [ ] Response contains paginated list

#### 7. Payment Statistics
- [ ] Endpoint: `GET /api/v1/payments/stats/user/{userId}`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8085/api/v1/payments/stats/user/5
```
- [ ] Response contains: totalPayments, totalAmount, completedPayments, etc.

#### 8. Update Payment Status (Admin)
- [ ] Endpoint: `PATCH /api/v1/payments/{paymentId}/status`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PATCH http://localhost:8085/api/v1/payments/101/status \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"FAILED","reason":"Card declined"}'
```
- [ ] Response status updated

---

### Delivery Service Endpoints

#### 1. Create Delivery
- [ ] Endpoint: `POST /api/v1/deliveries`
- [ ] Expected Status: 201 Created
- [ ] Test Command:
```bash
curl -X POST http://localhost:8086/api/v1/deliveries \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId":1,"kitchenId":2,"userId":5,"itemId":10,
    "estimatedDeliveryTime":"2026-02-03T12:00:00Z",
    "deliveryNotes":"Leave at gate"
  }'
```
- [ ] Response contains: `deliveryId`, `deliveryStatus: PENDING`

#### 2. Get Delivery by ID
- [ ] Endpoint: `GET /api/v1/deliveries/{deliveryId}`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8086/api/v1/deliveries/201
```
- [ ] Response returns delivery details

#### 3. Get Delivery by Order ID
- [ ] Endpoint: `GET /api/v1/deliveries/order/{orderId}`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8086/api/v1/deliveries/order/1
```
- [ ] Response returns delivery for order

#### 4. Assign Delivery Partner
- [ ] Endpoint: `PUT /api/v1/deliveries/{deliveryId}/assign`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/assign \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"partnerName":"Rajesh Kumar","partnerPhone":"9876543210"}'
```
- [ ] Response status: ASSIGNED

#### 5. Mark Pickup
- [ ] Endpoint: `PUT /api/v1/deliveries/{deliveryId}/pickup`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/pickup \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pickupTime":"2026-02-03T10:50:00Z","notes":"Picked up"}'
```
- [ ] Response status: PICKED_UP

#### 6. Mark In-Transit
- [ ] Endpoint: `PUT /api/v1/deliveries/{deliveryId}/in-transit`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/in-transit \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentLocation":"12.9716° N, 77.5946° E","notes":"On the way"}'
```
- [ ] Response status: IN_TRANSIT

#### 7. Complete Delivery
- [ ] Endpoint: `PUT /api/v1/deliveries/{deliveryId}/complete`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/complete \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"deliveryTime":"2026-02-03T11:45:00Z","notes":"Delivered","recipientName":"Smith"}'
```
- [ ] Response status: DELIVERED

#### 8. Mark Delivery Failed
- [ ] Endpoint: `PUT /api/v1/deliveries/{deliveryId}/failed`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -X PUT http://localhost:8086/api/v1/deliveries/201/failed \
  -H "Authorization: Bearer TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Customer not available","notes":"Will retry"}'
```
- [ ] Response status: FAILED

#### 9. Kitchen Deliveries
- [ ] Endpoint: `GET /api/v1/deliveries/kitchen/{kitchenId}`
- [ ] Expected Status: 200 OK
- [ ] Supports filtering: `?status=DELIVERED`
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  "http://localhost:8086/api/v1/deliveries/kitchen/2?page=0&size=10"
```
- [ ] Response contains paginated list

#### 10. User Deliveries
- [ ] Endpoint: `GET /api/v1/deliveries/user/{userId}`
- [ ] Expected Status: 200 OK
- [ ] Supports pagination
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  "http://localhost:8086/api/v1/deliveries/user/5?page=0&size=10"
```
- [ ] Response contains paginated list

#### 11. Partner Statistics
- [ ] Endpoint: `GET /api/v1/deliveries/partner/{partnerName}/stats`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8086/api/v1/deliveries/partner/Rajesh%20Kumar/stats
```
- [ ] Response contains partner statistics

#### 12. Overall Statistics
- [ ] Endpoint: `GET /api/v1/deliveries/stats`
- [ ] Expected Status: 200 OK
- [ ] Test Command:
```bash
curl -H "Authorization: Bearer TEST_TOKEN" \
  http://localhost:8086/api/v1/deliveries/stats
```
- [ ] Response contains system-wide statistics

---

## Error Handling Verification ✅

### Test Invalid Input
- [ ] Create payment with negative amount → 400 Bad Request
- [ ] Create payment with non-existent order → 404 Not Found
- [ ] Get payment with invalid ID → 404 Not Found
- [ ] Missing Authorization header → 401 Unauthorized
- [ ] Invalid JSON → 400 Bad Request

### Test Validation
- [ ] Payment amount validation works
- [ ] Phone number format validation works
- [ ] Required fields validation works
- [ ] Size constraints validation works

### Test Error Response Format
- [ ] Error response contains `status`, `error`, `message`
- [ ] Timestamp is included
- [ ] Path is included

---

## Database Verification ✅

### Payment Database
```sql
USE makan_payment_db;
SHOW TABLES;  -- Should show: payments
SELECT COUNT(*) FROM payments;  -- Should show created payments
SELECT * FROM payments LIMIT 1;  -- Verify structure
```

### Delivery Database
```sql
USE makan_delivery_db;
SHOW TABLES;  -- Should show: deliveries
SELECT COUNT(*) FROM deliveries;  -- Should show created deliveries
SELECT * FROM deliveries LIMIT 1;  -- Verify structure
```

### Indexes Verification
```sql
-- Payment indexes
SHOW INDEX FROM makan_payment_db.payments;
-- Should show: idx_order_id, idx_user_id, idx_payment_status, idx_payment_date

-- Delivery indexes
SHOW INDEX FROM makan_delivery_db.deliveries;
-- Should show: idx_order_id, idx_kitchen_id, idx_user_id, idx_delivery_status, idx_delivery_time
```

---

## Performance Testing ✅

### Response Time Verification
- [ ] GET /api/v1/payments/{id} < 100ms
- [ ] GET /api/v1/deliveries/{id} < 100ms
- [ ] POST /api/v1/payments < 200ms
- [ ] POST /api/v1/deliveries < 200ms
- [ ] GET /api/v1/payments/user/{id}?page=0&size=10 < 500ms
- [ ] GET /api/v1/deliveries/user/{id}?page=0&size=10 < 500ms

### Load Testing
- [ ] Create 100 payments without errors
- [ ] Create 100 deliveries without errors
- [ ] List operations with large datasets (pagination works)
- [ ] Concurrent requests (multiple simultaneous operations)

---

## Integration Testing ✅

### Cross-Service Communication
- [ ] Payment Service → Order Service lookup works
- [ ] Delivery Service → Order Service lookup works
- [ ] Foreign key constraints enforced
- [ ] Status transitions are valid

### Event Emission (if implemented)
- [ ] Payment created event published
- [ ] Payment completed event published
- [ ] Delivery created event published
- [ ] Delivery completed event published

---

## Monitoring Verification ✅

### Health Checks
```bash
# Payment Service
curl http://localhost:8085/actuator/health

# Delivery Service
curl http://localhost:8086/actuator/health
```
Expected: `{"status":"UP"}`

### Metrics Collection
```bash
# Payment Service Metrics
curl http://localhost:8085/actuator/metrics

# Delivery Service Metrics
curl http://localhost:8086/actuator/metrics
```

### Logs Verification
- [ ] Logs created in `logs/` directory
- [ ] No ERROR level logs
- [ ] Application startup logged
- [ ] Request/response logged (if enabled)

---

## Production Readiness ✅

### Security
- [ ] JWT secret key changed from default
- [ ] Database credentials not hardcoded
- [ ] HTTPS configured (if applicable)
- [ ] CORS configured appropriately
- [ ] Rate limiting configured

### Reliability
- [ ] Connection pooling configured
- [ ] Timeout values set appropriately
- [ ] Retry logic implemented (if needed)
- [ ] Error handling comprehensive
- [ ] Logging configured

### Scalability
- [ ] Database indexes created
- [ ] Batch processing configured
- [ ] Pagination working
- [ ] Caching configured (if applicable)
- [ ] Load tested

### Maintainability
- [ ] Code documented
- [ ] APIs documented
- [ ] Setup documented
- [ ] Troubleshooting guide available
- [ ] Version control configured

---

## Post-Deployment Checklist ✅

- [ ] Services running without errors
- [ ] All endpoints verified working
- [ ] Database connectivity verified
- [ ] Logs reviewed and clean
- [ ] Performance metrics acceptable
- [ ] Monitoring configured
- [ ] Alerting configured
- [ ] Backup/recovery plan documented
- [ ] Runbook created
- [ ] Team trained on operations

---

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| QA Engineer | | | |
| DevOps Engineer | | | |
| Project Manager | | | |

---

## Notes

Use this checklist during each deployment phase:
1. **Development**: Local testing checklist
2. **Testing**: Full API and integration testing
3. **Staging**: Production-like environment verification
4. **Production**: Final pre-launch verification

---

**Last Updated**: February 3, 2026  
**Version**: 1.0  
**Status**: Ready for Use
