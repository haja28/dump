# Makan For You - Spring Boot Microservices API Documentation

## Overview
This is a comprehensive Spring Boot microservices backend for a home-cooked food marketplace Flutter application. The architecture includes:
- **API Gateway** - Central entry point with JWT authentication
- **Auth Service** - User registration, login, and token management
- **Kitchen Service** - Kitchen registration and profile management
- **Menu Service** - Menu items, labels, advanced search and filtering
- **Order Service** - Order creation, management, and tracking

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Build Tool**: Maven
- **Database**: MySQL 8.0+
- **Cache**: Redis
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: Swagger/OpenAPI 3.0
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation

### Microservices Overview

#### 1. API Gateway (Port 8080)
- Routes requests to appropriate services
- JWT token validation for protected endpoints
- Rate limiting and request logging
- **Public Endpoints**: Auth, Kitchen browsing, Menu search

#### 2. Auth Service (Port 8081)
- User registration with email and phone number validation
- User login with JWT token generation
- Token refresh mechanism
- User role management (CUSTOMER, KITCHEN, ADMIN)

#### 3. Kitchen Service (Port 8082)
- Kitchen registration by home chefs
- Kitchen profile management
- Admin approval workflow
- Kitchen search and filtering by location

#### 4. Menu Service (Port 8083)
- Menu item CRUD operations
- Advanced search with multiple filters
- Label/tag system for categorization
- Redis caching for performance
- Support for dietary preferences (veg, halal)

#### 5. Order Service (Port 8084)
- Order creation and management
- Order status tracking
- Kitchen confirmation workflow
- Order history retrieval

## Installation & Setup

### Prerequisites
- Java 17+
- MySQL 8.0+
- Redis (optional, required for menu caching)
- Maven 3.8+

### Database Setup

Create databases for each service:
```sql
CREATE DATABASE makan_auth_db;
CREATE DATABASE makan_kitchen_db;
CREATE DATABASE makan_menu_db;
CREATE DATABASE makan_order_db;
```

### Configuration

Update database credentials in each service's `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_[service]_db
    username: root
    password: your_password
```

Update JWT secret in all services:
```yaml
jwt:
  secret: your-256-bit-secret-key
```

### Running Services

1. **Start Auth Service**
```bash
cd auth-service
mvn spring-boot:run
```

2. **Start Kitchen Service**
```bash
cd kitchen-service
mvn spring-boot:run
```

3. **Start Menu Service**
```bash
cd menu-service
mvn spring-boot:run
```

4. **Start Order Service**
```bash
cd order-service
mvn spring-boot:run
```

5. **Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "password": "SecurePassword123",
  "role": "CUSTOMER"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phoneNumber": "9876543210",
      "role": "CUSTOMER",
      "isActive": true,
      "createdAt": "2026-01-30T10:15:00Z"
    }
  },
  "message": "User registered successfully",
  "timestamp": "2026-01-30T10:15:00Z"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword123"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 900000,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phoneNumber": "9876543210",
      "role": "CUSTOMER",
      "isActive": true,
      "lastLogin": "2026-01-30T10:15:00Z"
    }
  },
  "message": "Login successful",
  "timestamp": "2026-01-30T10:15:00Z"
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}

Response: 200 OK
```

### Kitchen Endpoints

#### Register Kitchen
```http
POST /api/v1/kitchens/register
Authorization: Bearer {accessToken}
X-User-Id: 1
Content-Type: application/json

{
  "kitchenName": "Grandma's Kitchen",
  "description": "Home-cooked authentic meals",
  "address": "123 Main St, Apt 4B",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "deliveryArea": "Manhattan",
  "cuisineTypes": "Indian,Continental",
  "ownerContact": "9876543210",
  "ownerEmail": "owner@example.com",
  "alternateContact": "9876543211",
  "alternateEmail": "alternate@example.com"
}

Response: 201 Created
```

#### Get Approved Kitchens
```http
GET /api/v1/kitchens?approved=true&page=0&size=10

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "kitchenName": "Grandma's Kitchen",
        "approvalStatus": "APPROVED",
        "rating": 4.5,
        "totalOrders": 150,
        "isActive": true,
        "verified": true,
        ...
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "timestamp": "2026-01-30T10:15:00Z"
}
```

#### Search Kitchens
```http
GET /api/v1/kitchens/search?query=biryani&page=0&size=10

Response: 200 OK
```

#### Get Kitchen by City
```http
GET /api/v1/kitchens/by-city/NewYork?page=0&size=10

Response: 200 OK
```

### Menu Endpoints

#### Create Menu Item
```http
POST /api/v1/menu-items
Authorization: Bearer {accessToken}
X-Kitchen-Id: 1
Content-Type: application/json

{
  "itemName": "Biryani",
  "description": "Authentic Hyderabadi biryani",
  "ingredients": "Rice, Chicken, Spices",
  "allergyIndication": "Contains nuts",
  "cost": 12.99,
  "imagePath": "/images/biryani.jpg",
  "availableTiming": "12:00 PM - 9:00 PM",
  "preparationTimeMinutes": 30,
  "quantityAvailable": 50,
  "isVeg": false,
  "isHalal": true,
  "spicyLevel": 4,
  "labelIds": [1, 2, 3]
}

Response: 201 Created
```

#### Search Menu Items (Advanced)
```http
GET /api/v1/menu-items/search?query=biryani&kitchenId=1&labels=halal,spicy&minPrice=10&maxPrice=20&veg=false&halal=true&sort=rating_desc&page=0&size=10

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "kitchenId": 1,
        "itemName": "Biryani",
        "cost": 12.99,
        "isVeg": false,
        "isHalal": true,
        "spicyLevel": 4,
        "rating": 4.8,
        "labels": [
          {
            "id": 1,
            "name": "halal",
            "isActive": true
          },
          {
            "id": 2,
            "name": "spicy",
            "isActive": true
          }
        ],
        ...
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 5,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  },
  "timestamp": "2026-01-30T10:15:00Z"
}
```

#### Get Kitchen Menu
```http
GET /api/v1/menu-items/kitchen/1?page=0&size=20

Response: 200 OK
```

#### Create Menu Label
```http
POST /api/v1/menu-labels?name=vegan&description=Vegan friendly
Authorization: Bearer {accessToken}

Response: 201 Created
```

#### Get All Labels
```http
GET /api/v1/menu-labels

Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "vegan",
      "description": "Vegan friendly",
      "isActive": true
    },
    {
      "id": 2,
      "name": "halal",
      "description": "Halal certified",
      "isActive": true
    },
    ...
  ]
}
```

### Order Endpoints

#### Create Order
```http
POST /api/v1/orders
Authorization: Bearer {accessToken}
X-User-Id: 1
Content-Type: application/json

{
  "kitchenId": 1,
  "deliveryAddress": "456 Main St, Apt 5B",
  "deliveryCity": "New York",
  "deliveryState": "NY",
  "deliveryPostalCode": "10002",
  "specialInstructions": "Extra spicy, no onions",
  "items": [
    {
      "itemId": 5,
      "quantity": 2,
      "specialRequests": "Extra rice"
    },
    {
      "itemId": 8,
      "quantity": 1,
      "specialRequests": null
    }
  ]
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 100,
    "userId": 1,
    "kitchenId": 1,
    "orderTotal": 28.98,
    "orderStatus": "PENDING",
    "confirmationByKitchen": false,
    "deliveryAddress": "456 Main St, Apt 5B",
    "items": [
      {
        "id": 200,
        "orderId": 100,
        "itemId": 5,
        "itemQuantity": 2,
        "itemUnitPrice": 12.99,
        "itemTotal": 25.98,
        "specialRequests": "Extra rice"
      }
    ],
    "createdAt": "2026-01-30T10:15:00Z"
  },
  "message": "Order created successfully"
}
```

#### Get My Orders
```http
GET /api/v1/orders/my-orders?page=0&size=10
Authorization: Bearer {accessToken}
X-User-Id: 1

Response: 200 OK
```

#### Get Kitchen Orders
```http
GET /api/v1/orders/kitchen/1?page=0&size=10
Authorization: Bearer {accessToken}
X-Kitchen-Id: 1

Response: 200 OK
```

#### Get Pending Orders
```http
GET /api/v1/orders/kitchen/1/pending?page=0&size=10
Authorization: Bearer {accessToken}
X-Kitchen-Id: 1

Response: 200 OK
```

#### Accept Order
```http
PATCH /api/v1/orders/100/accept
Authorization: Bearer {accessToken}
X-Kitchen-Id: 1

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 100,
    "orderStatus": "CONFIRMED",
    "confirmationByKitchen": true,
    "confirmationTimestamp": "2026-01-30T10:20:00Z"
  }
}
```

#### Update Order Status
```http
PATCH /api/v1/orders/100/status?status=READY
Authorization: Bearer {accessToken}

Response: 200 OK
```

#### Cancel Order
```http
PATCH /api/v1/orders/100/cancel
Authorization: Bearer {accessToken}
X-User-Id: 1

Response: 200 OK
```

## Swagger/OpenAPI Documentation

Each service has Swagger UI available at:
- Auth Service: http://localhost:8081/swagger-ui.html
- Kitchen Service: http://localhost:8082/swagger-ui.html
- Menu Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html
- API Gateway: http://localhost:8080/swagger-ui.html

## Standard Response Format

All responses follow this format:

```json
{
  "success": true,
  "data": {},
  "message": "Operation successful",
  "timestamp": "2026-01-30T10:15:00Z",
  "errors": []
}
```

## Error Handling

Standard error response:
```json
{
  "success": false,
  "message": "Error message",
  "timestamp": "2026-01-30T10:15:00Z",
  "errors": ["Field validation error 1", "Field validation error 2"]
}
```

## Security

### JWT Token Structure
- **Access Token**: 15 minutes expiration
- **Refresh Token**: 7 days expiration
- **Header**: `Authorization: Bearer {token}`

### Protected Endpoints
All endpoints except those listed below require valid JWT token:
- `/api/v1/auth/register`
- `/api/v1/auth/login`
- `/api/v1/auth/refresh`
- `/api/v1/kitchens` (GET only)
- `/api/v1/menu-items/search` (GET only)

### User Headers
After authentication, the gateway adds these headers to requests:
- `X-User-Id`: User ID
- `X-User-Email`: User email
- `X-User-Role`: User role (CUSTOMER, KITCHEN, ADMIN)

## Caching Strategy

- **Menu Items**: Cached in Redis for 1 hour
- **Kitchen Data**: Cached in Redis for 30 minutes
- **Labels**: Cached in Redis for 12 hours

## Database Schema

Refer to `database_schema.sql` for complete schema documentation.

## Future Enhancements

1. **Payment Service**: Integration with payment gateway
2. **Delivery Tracking**: Real-time delivery updates
3. **Reviews & Ratings**: Customer feedback system
4. **Notifications**: Email and SMS notifications
5. **Analytics**: Dashboard for kitchens and admin
6. **Message Service**: Customer-kitchen communication

## Support & Troubleshooting

For common issues and solutions, refer to the TROUBLESHOOTING.md file.
