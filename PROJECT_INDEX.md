# Makan For You - Complete Project Index

## 📑 Project Structure & Files

```
makanforyou/
│
├── 📄 pom.xml                           # Parent Maven configuration
│
├── 📄 README.md                         # Main project documentation
├── 📄 API_DOCUMENTATION.md              # Complete API reference
├── 📄 SETUP_AND_DEPLOYMENT.md          # Setup and deployment guide
├── 📄 TROUBLESHOOTING.md               # Troubleshooting guide
├── 📄 DELIVERABLES.md                  # Complete deliverables checklist
├── 📄 QUICK_REFERENCE.md               # Quick reference guide
├── 📄 database_schema.sql              # Complete database schema
│
├── 📁 common/                          # Shared module
│   ├── pom.xml
│   └── src/main/java/com/makanforyou/common/
│       ├── dto/
│       │   ├── ApiResponse.java        # Standard response wrapper
│       │   ├── PagedResponse.java      # Paginated response wrapper
│       │   └── PaginationMetadata.java # Pagination info
│       └── exception/
│           └── ApplicationException.java # Custom exception base
│
├── 📁 api-gateway/                     # Central API Gateway
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml             # Gateway configuration
│   └── src/main/java/com/makanforyou/gateway/
│       ├── ApiGatewayApplication.java  # Main application
│       └── filter/
│           └── JwtAuthenticationFilter.java # JWT validation
│
├── 📁 auth-service/                    # Authentication Service
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml             # Service configuration
│   └── src/main/java/com/makanforyou/auth/
│       ├── AuthServiceApplication.java # Main application
│       ├── controller/
│       │   └── AuthController.java     # REST endpoints
│       ├── service/
│       │   └── AuthService.java        # Business logic
│       ├── entity/
│       │   └── User.java               # User entity
│       ├── dto/
│       │   ├── UserDTO.java
│       │   ├── RegisterRequest.java
│       │   ├── LoginRequest.java
│       │   ├── AuthResponse.java
│       │   └── RefreshTokenRequest.java
│       ├── repository/
│       │   └── UserRepository.java     # Data access
│       ├── security/
│       │   └── JwtTokenProvider.java   # JWT handling
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── JpaConfig.java
│       │   └── WebConfig.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
├── 📁 kitchen-service/                 # Kitchen Management Service
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml             # Service configuration
│   └── src/main/java/com/makanforyou/kitchen/
│       ├── KitchenServiceApplication.java
│       ├── controller/
│       │   └── KitchenController.java  # REST endpoints
│       ├── service/
│       │   └── KitchenService.java     # Business logic
│       ├── entity/
│       │   └── Kitchen.java            # Kitchen entity
│       ├── dto/
│       │   ├── KitchenDTO.java
│       │   └── KitchenRegistrationRequest.java
│       ├── repository/
│       │   └── KitchenRepository.java  # Data access
│       ├── config/
│       │   └── JpaConfig.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
├── 📁 menu-service/                    # Menu Item Service
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml             # Service configuration
│   └── src/main/java/com/makanforyou/menu/
│       ├── MenuServiceApplication.java
│       ├── controller/
│       │   ├── MenuItemController.java # Menu item endpoints
│       │   └── MenuLabelController.java # Label endpoints
│       ├── service/
│       │   ├── MenuItemService.java    # Menu business logic
│       │   └── MenuLabelService.java   # Label business logic
│       ├── entity/
│       │   ├── MenuItem.java           # Menu item entity
│       │   └── MenuLabel.java          # Label entity
│       ├── dto/
│       │   ├── MenuItemDTO.java
│       │   ├── MenuItemRequest.java
│       │   ├── MenuLabelDTO.java
│       │   └── MenuSearchFilter.java   # Search filter object
│       ├── repository/
│       │   ├── MenuItemRepository.java # Menu queries
│       │   └── MenuLabelRepository.java # Label queries
│       ├── specification/
│       │   └── MenuItemSpecification.java # JPA specifications
│       ├── config/
│       │   ├── JpaConfig.java
│       │   └── RedisConfig.java        # Redis caching
│       └── exception/
│           └── GlobalExceptionHandler.java
│
├── 📁 order-service/                   # Order Management Service
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml             # Service configuration
│   └── src/main/java/com/makanforyou/order/
│       ├── OrderServiceApplication.java
│       ├── controller/
│       │   └── OrderController.java    # REST endpoints
│       ├── service/
│       │   └── OrderService.java       # Business logic
│       ├── entity/
│       │   ├── Order.java              # Order entity
│       │   └── OrderItem.java          # Order item entity
│       ├── dto/
│       │   ├── OrderDTO.java
│       │   ├── OrderItemDTO.java
│       │   ├── CreateOrderRequest.java
│       │   └── OrderItemRequest.java
│       ├── repository/
│       │   ├── OrderRepository.java    # Order data access
│       │   └── OrderItemRepository.java # Order item data access
│       ├── config/
│       │   └── JpaConfig.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
└── 📄 .gitignore                       # Git ignore file
```

---

## 📦 Module Dependency Graph

```
┌─────────────────────────────────────────────┐
│         makanforyou (Parent POM)            │
├─────────────────────────────────────────────┤
│  ├─ common (shared utilities)               │
│  ├─ auth-service (depends on common)        │
│  ├─ kitchen-service (depends on common)     │
│  ├─ menu-service (depends on common)        │
│  ├─ order-service (depends on common)       │
│  └─ api-gateway (routes to all services)    │
└─────────────────────────────────────────────┘
```

---

## 🗂️ File Breakdown by Type

### Configuration Files (YAML)
```
api-gateway/src/main/resources/application.yml
auth-service/src/main/resources/application.yml
kitchen-service/src/main/resources/application.yml
menu-service/src/main/resources/application.yml
order-service/src/main/resources/application.yml
```

### Build Files (POM)
```
pom.xml (parent)
common/pom.xml
auth-service/pom.xml
kitchen-service/pom.xml
menu-service/pom.xml
order-service/pom.xml
api-gateway/pom.xml
```

### Database Files (SQL)
```
database_schema.sql (complete schema with all tables and indexes)
```

### Documentation Files (Markdown)
```
README.md
API_DOCUMENTATION.md
SETUP_AND_DEPLOYMENT.md
TROUBLESHOOTING.md
DELIVERABLES.md
QUICK_REFERENCE.md
```

---

## 📊 Java Classes by Service

### API Gateway (1 service, 2 classes)
- `ApiGatewayApplication.java`
- `JwtAuthenticationFilter.java`

### Auth Service (5 endpoints, 13 classes)
**Controllers (1)**
- `AuthController.java`

**Services (1)**
- `AuthService.java`

**Entities (1)**
- `User.java`

**DTOs (5)**
- `UserDTO.java`
- `RegisterRequest.java`
- `LoginRequest.java`
- `AuthResponse.java`
- `RefreshTokenRequest.java`

**Repositories (1)**
- `UserRepository.java`

**Security (1)**
- `JwtTokenProvider.java`

**Configuration (3)**
- `SecurityConfig.java`
- `JpaConfig.java`
- `WebConfig.java`

**Exception Handling (1)**
- `GlobalExceptionHandler.java`

### Kitchen Service (9 endpoints, 10 classes)
**Controllers (1)**
- `KitchenController.java`

**Services (1)**
- `KitchenService.java`

**Entities (1)**
- `Kitchen.java`

**DTOs (2)**
- `KitchenDTO.java`
- `KitchenRegistrationRequest.java`

**Repositories (1)**
- `KitchenRepository.java`

**Configuration (1)**
- `JpaConfig.java`

**Exception Handling (1)**
- `GlobalExceptionHandler.java`

### Menu Service (11 endpoints, 17 classes)
**Controllers (2)**
- `MenuItemController.java`
- `MenuLabelController.java`

**Services (2)**
- `MenuItemService.java`
- `MenuLabelService.java`

**Entities (2)**
- `MenuItem.java`
- `MenuLabel.java`

**DTOs (4)**
- `MenuItemDTO.java`
- `MenuItemRequest.java`
- `MenuLabelDTO.java`
- `MenuSearchFilter.java`

**Repositories (2)**
- `MenuItemRepository.java`
- `MenuLabelRepository.java`

**Specification (1)**
- `MenuItemSpecification.java`

**Configuration (2)**
- `JpaConfig.java`
- `RedisConfig.java`

**Exception Handling (1)**
- `GlobalExceptionHandler.java`

### Order Service (8 endpoints, 14 classes)
**Controllers (1)**
- `OrderController.java`

**Services (1)**
- `OrderService.java`

**Entities (2)**
- `Order.java`
- `OrderItem.java`

**DTOs (4)**
- `OrderDTO.java`
- `OrderItemDTO.java`
- `CreateOrderRequest.java`
- `OrderItemRequest.java`

**Repositories (2)**
- `OrderRepository.java`
- `OrderItemRepository.java`

**Configuration (1)**
- `JpaConfig.java`

**Exception Handling (1)**
- `GlobalExceptionHandler.java`

### Common Module (4 classes)
**DTOs (3)**
- `ApiResponse.java`
- `PagedResponse.java`
- `PaginationMetadata.java`

**Exception (1)**
- `ApplicationException.java`

---

## 🔢 Code Statistics

| Category | Count |
|----------|-------|
| **Microservices** | 5 |
| **Total Java Classes** | 67 |
| **Total Endpoints** | 33 |
| **Database Tables** | 9 |
| **Configuration Files** | 6 |
| **POM Files** | 7 |
| **Documentation Files** | 6 |
| **SQL Scripts** | 1 |

---

## 🔄 Service Communication

```
Flutter App
    ↓ (Port 8080)
API Gateway
    ├─→ Auth Service (8081)
    │   └─ MySQL: makan_auth_db
    │
    ├─→ Kitchen Service (8082)
    │   └─ MySQL: makan_kitchen_db
    │
    ├─→ Menu Service (8083)
    │   ├─ MySQL: makan_menu_db
    │   └─ Redis Cache
    │
    └─→ Order Service (8084)
        └─ MySQL: makan_order_db
```

---

## 📝 Database Tables

1. **users** - User authentication and profiles
2. **kitchens** - Kitchen profiles and registration
3. **kitchen_menu** - Menu items with labels
4. **menu_labels** - Label/tag definitions
5. **menu_item_labels** - Junction table for item-label relationship
6. **orders** - Customer orders
7. **order_items** - Order line items
8. **payments** - Payment records
9. **deliveries** - Delivery tracking

---

## 🎯 API Endpoints Summary

### Auth Endpoints (5)
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- GET /api/v1/auth/me
- GET /api/v1/auth/users/{userId}

### Kitchen Endpoints (9)
- POST /api/v1/kitchens/register
- GET /api/v1/kitchens/{id}
- GET /api/v1/kitchens
- GET /api/v1/kitchens/search
- GET /api/v1/kitchens/by-city/{city}
- GET /api/v1/kitchens/my-kitchen
- PUT /api/v1/kitchens/{id}
- PATCH /api/v1/kitchens/{id}/approve
- PATCH /api/v1/kitchens/{id}/reject
- PATCH /api/v1/kitchens/{id}/deactivate

### Menu Endpoints (11)
- POST /api/v1/menu-items
- GET /api/v1/menu-items/{id}
- GET /api/v1/menu-items/search
- GET /api/v1/menu-items/kitchen/{id}
- PUT /api/v1/menu-items/{id}
- PATCH /api/v1/menu-items/{id}/deactivate
- POST /api/v1/menu-labels
- GET /api/v1/menu-labels
- GET /api/v1/menu-labels/{id}
- PUT /api/v1/menu-labels/{id}
- PATCH /api/v1/menu-labels/{id}/deactivate

### Order Endpoints (8)
- POST /api/v1/orders
- GET /api/v1/orders/{id}
- GET /api/v1/orders/my-orders
- GET /api/v1/orders/kitchen/{id}
- GET /api/v1/orders/kitchen/{id}/pending
- PATCH /api/v1/orders/{id}/accept
- PATCH /api/v1/orders/{id}/status
- PATCH /api/v1/orders/{id}/cancel

---

## 📚 How to Use This Index

1. **Find a specific file**: Search this document for the file path
2. **Understand structure**: See the project tree at the top
3. **Locate endpoints**: Check API Endpoints Summary section
4. **Count resources**: Use Code Statistics section
5. **Trace dependencies**: See Module Dependency Graph

---

## 🚀 Next Steps

1. **Start here**: Read README.md
2. **Setup project**: Follow SETUP_AND_DEPLOYMENT.md
3. **Test APIs**: Use QUICK_REFERENCE.md
4. **Troubleshoot**: Check TROUBLESHOOTING.md
5. **Deep dive**: Read API_DOCUMENTATION.md

---

## ✅ Verification Checklist

Use this to verify project completeness:

- [ ] All 5 microservices present
- [ ] All 33 endpoints implemented
- [ ] Common module with shared DTOs
- [ ] All 6 documentation files present
- [ ] Database schema SQL file present
- [ ] JWT authentication configured
- [ ] Redis caching configured
- [ ] Exception handling in all services
- [ ] Pagination support in list endpoints
- [ ] Advanced search/filtering in menu service

---

**Project Status**: ✅ COMPLETE
**Last Updated**: January 30, 2026
**Version**: 1.0.0
