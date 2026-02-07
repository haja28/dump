# Makan For You - Home Cooked Food Marketplace Backend

A production-ready Spring Boot microservices backend for a home-cooked food marketplace Flutter application. This platform empowers home chefs to register their kitchens, manage menus, and connect with customers seeking authentic home-cooked meals.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Microservices](#microservices)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Security](#security)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

### Customer Features
- ✅ User registration with email and phone number validation
- ✅ Secure login with JWT authentication
- ✅ Browse approved kitchens
- ✅ Advanced search and filtering for menu items
- ✅ Filter by labels (veg, halal, spicy, homemade, etc.)
- ✅ Filter by price range and dietary preferences
- ✅ Place orders with multiple items
- ✅ View order history and status tracking
- ✅ Cancel orders

### Kitchen Owner Features
- ✅ Register home kitchen with details
- ✅ Add and manage menu items
- ✅ Set availability timing
- ✅ Assign labels to menu items
- ✅ Manage kitchen profile
- ✅ Accept/reject orders
- ✅ Update order status
- ✅ View kitchen analytics (orders, ratings)

### Admin Features
- ✅ Approve/reject kitchen registrations
- ✅ Manage menu labels and categories
- ✅ Monitor all orders
- ✅ User and kitchen management

## 🏗️ Architecture

The application follows a **microservices architecture** with the following components:

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Port 8080)                  │
│              (JWT Validation, Request Routing)                │
└──────────────┬──────────────┬──────────────┬─────────────────┘
               │              │              │
        ┌──────▼──┐    ┌──────▼──┐    ┌─────▼────┐    ┌────────┐
        │ Auth     │    │ Kitchen  │    │ Menu     │    │ Order  │
        │ Service  │    │ Service  │    │ Service  │    │ Service│
        │ :8081    │    │ :8082    │    │ :8083    │    │ :8084  │
        └──────────┘    └──────────┘    └──────────┘    └────────┘
             │              │              │ (Redis)          │
             └──────────────┴──────────────┴──────────────────┘
                            │
                     ┌──────▼──────┐
                     │   MySQL     │
                     │   Database  │
                     └─────────────┘
```

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 3.2.0 |
| **Language** | Java 17+ |
| **Build Tool** | Maven 3.8+ |
| **Database** | MySQL 8.0+ |
| **Cache** | Redis 6.0+ |
| **Authentication** | JWT (JSON Web Tokens) |
| **API Gateway** | Spring Cloud Gateway |
| **ORM** | Spring Data JPA |
| **API Documentation** | Swagger/OpenAPI 3.0 |
| **Validation** | Jakarta Validation |
| **Containerization** | Docker |
| **Orchestration** | Kubernetes (optional) |

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+ (optional)
- Git

### Installation

1. **Clone Repository**
```bash
git clone https://github.com/your-org/makanforyou.git
cd makanforyou
```

2. **Setup Databases**
```bash
# Create databases
mysql -u root -p < database_schema.sql
```

3. **Configure Services**
Update credentials in each service's `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makan_[service]_db
    username: your_username
    password: your_password

jwt:
  secret: your_jwt_secret_key
```

4. **Build Project**
```bash
mvn clean install
```

5. **Run Services**
```bash
# Terminal 1
cd auth-service && mvn spring-boot:run

# Terminal 2
cd kitchen-service && mvn spring-boot:run

# Terminal 3
cd menu-service && mvn spring-boot:run

# Terminal 4
cd order-service && mvn spring-boot:run

# Terminal 5
cd api-gateway && mvn spring-boot:run
```

6. **Access Services**
- API Gateway: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Individual Services: http://localhost:808X/swagger-ui.html (where X is 1-4)

## 📁 Project Structure

```
makanforyou/
├── api-gateway/                    # API Gateway Service
│   ├── src/main/java/
│   │   └── com/makanforyou/gateway/
│   │       ├── filter/            # JWT Authentication Filter
│   │       └── ApiGatewayApplication.java
│   └── src/main/resources/
│       └── application.yml
│
├── auth-service/                   # Authentication Service
│   ├── src/main/java/
│   │   └── com/makanforyou/auth/
│   │       ├── controller/        # Auth endpoints
│   │       ├── service/           # Business logic
│   │       ├── entity/            # User entity
│   │       ├── dto/               # Data transfer objects
│   │       ├── repository/        # Data access
│   │       ├── security/          # JWT provider
│   │       ├── config/            # Configuration
│   │       └── exception/         # Exception handling
│   └── pom.xml
│
├── kitchen-service/                # Kitchen Management Service
│   ├── src/main/java/
│   │   └── com/makanforyou/kitchen/
│   │       ├── controller/        # Kitchen endpoints
│   │       ├── service/           # Business logic
│   │       ├── entity/            # Kitchen entity
│   │       ├── dto/               # Data transfer objects
│   │       ├── repository/        # Data access
│   │       ├── config/            # Configuration
│   │       └── exception/         # Exception handling
│   └── pom.xml
│
├── menu-service/                   # Menu Item Service
│   ├── src/main/java/
│   │   └── com/makanforyou/menu/
│   │       ├── controller/        # Menu endpoints
│   │       ├── service/           # Business logic
│   │       ├── entity/            # MenuItem, MenuLabel entities
│   │       ├── dto/               # Data transfer objects
│   │       ├── repository/        # Data access
│   │       ├── specification/     # JPA Specifications for filtering
│   │       ├── config/            # Configuration (JPA, Redis)
│   │       └── exception/         # Exception handling
│   └── pom.xml
│
├── order-service/                  # Order Management Service
│   ├── src/main/java/
│   │   └── com/makanforyou/order/
│   │       ├── controller/        # Order endpoints
│   │       ├── service/           # Business logic
│   │       ├── entity/            # Order, OrderItem entities
│   │       ├── dto/               # Data transfer objects
│   │       ├── repository/        # Data access
│   │       ├── config/            # Configuration
│   │       └── exception/         # Exception handling
│   └── pom.xml
│
├── common/                         # Shared Module
│   ├── src/main/java/
│   │   └── com/makanforyou/common/
│   │       ├── dto/               # ApiResponse, PagedResponse
│   │       └── exception/         # ApplicationException
│   └── pom.xml
│
├── database_schema.sql            # Complete database schema
├── pom.xml                        # Parent POM
├── README.md                      # This file
├── API_DOCUMENTATION.md           # Complete API documentation
├── SETUP_AND_DEPLOYMENT.md       # Setup and deployment guide
└── TROUBLESHOOTING.md            # Common issues and solutions
```

## 📚 API Documentation

### Complete API Reference
See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for:
- Detailed endpoint descriptions
- Request/response examples
- Authentication flow
- Error handling
- Pagination
- Filtering options

### Swagger/OpenAPI
Access interactive API documentation at:
- Gateway: http://localhost:8080/swagger-ui.html
- Individual Services: http://localhost:808X/swagger-ui.html

### Quick Examples

**Register User:**
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

**Search Menu Items:**
```bash
curl "http://localhost:8080/api/v1/menu-items/search?query=biryani&minPrice=5&maxPrice=20&halal=true&sort=rating_desc"
```

**Create Order:**
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "kitchenId": 1,
    "deliveryAddress": "123 Main St",
    "items": [
      {"itemId": 5, "quantity": 2}
    ]
  }'
```

## 🔧 Microservices

### Auth Service (Port 8081)
Handles user authentication and authorization.
- User registration
- User login
- Token refresh
- Role management

**Key Endpoints:**
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh token
- `GET /api/v1/auth/me` - Get current user

### Kitchen Service (Port 8082)
Manages home kitchen registration and profiles.
- Kitchen registration
- Profile management
- Admin approval workflow
- Location-based search

**Key Endpoints:**
- `POST /api/v1/kitchens/register` - Register kitchen
- `GET /api/v1/kitchens/{id}` - Get kitchen details
- `GET /api/v1/kitchens` - Browse kitchens
- `GET /api/v1/kitchens/by-city/{city}` - Search by location

### Menu Service (Port 8083)
Manages menu items and advanced search/filtering.
- Menu item CRUD
- Advanced search
- Label management
- Redis caching

**Key Endpoints:**
- `POST /api/v1/menu-items` - Create menu item
- `GET /api/v1/menu-items/search` - Advanced search
- `GET /api/v1/menu-labels` - Get all labels
- `POST /api/v1/menu-labels` - Create label

### Order Service (Port 8084)
Manages customer orders and order lifecycle.
- Order creation
- Status tracking
- Kitchen confirmation
- Order history

**Key Endpoints:**
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders/my-orders` - Get user's orders
- `GET /api/v1/orders/kitchen/{id}` - Get kitchen orders
- `PATCH /api/v1/orders/{id}/accept` - Accept order

## 🗄️ Database Schema

Complete database schema is documented in [database_schema.sql](./database_schema.sql) including:
- Users table
- Kitchens table
- Menu items and labels
- Orders and order items
- Reviews and ratings
- Full-text search indexes

All tables include:
- Proper foreign key relationships
- Optimized indexes for queries
- Timestamp tracking (createdAt, updatedAt)
- Status enums

## ⚙️ Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/makan_auth_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

### Application Properties
Each service has an `application.yml` file for configuration:
```yaml
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/makan_auth_db
    username: root
    password: root
  jpa:
    hibernate.ddl-auto: update
    show-sql: false

server:
  port: 8081

jwt:
  secret: your_secret_key
  access-token:
    expiration: 900000
```

## 🔐 Security

### Authentication Flow
1. User registers or logs in
2. Auth Service validates credentials and generates JWT tokens
3. Client stores access and refresh tokens
4. API Gateway validates JWT for protected endpoints
5. User info added to request headers (X-User-Id, X-User-Role)

### JWT Token
- **Access Token**: 15 minutes expiration
- **Refresh Token**: 7 days expiration
- **Algorithm**: HS512 with secret key

### Protected Endpoints
Require valid JWT token in Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Public Endpoints
No authentication required:
- `/api/v1/auth/register`
- `/api/v1/auth/login`
- `/api/v1/auth/refresh`
- `/api/v1/kitchens` (GET only)
- `/api/v1/menu-items/search` (GET only)

## 🚀 Deployment

### Docker Deployment
```bash
docker-compose up -d
```

### Kubernetes Deployment
```bash
kubectl apply -f k8s/
```

For detailed setup and deployment instructions, see [SETUP_AND_DEPLOYMENT.md](./SETUP_AND_DEPLOYMENT.md)

## 📝 Standard Response Format

All API responses follow this format:

**Success Response:**
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation successful",
  "timestamp": "2026-01-30T10:15:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error message",
  "errors": ["Field validation error 1", "Field validation error 2"],
  "timestamp": "2026-01-30T10:15:00Z"
}
```

**Paginated Response:**
```json
{
  "success": true,
  "data": {
    "content": [/* array of items */],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

## 🔍 Troubleshooting

For common issues and solutions, see [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

Quick links:
- [Database Connection Issues](./TROUBLESHOOTING.md#database-connection-issues)
- [Authentication Issues](./TROUBLESHOOTING.md#authentication-issues)
- [Port Already in Use](./TROUBLESHOOTING.md#port-already-in-use)
- [Performance Issues](./TROUBLESHOOTING.md#performance-issues)

## 📊 Performance & Caching

### Redis Caching
- Menu items: 1 hour TTL
- Kitchen data: 30 minutes TTL
- Menu labels: 12 hours TTL

### Database Indexes
- Kitchen name and city
- Menu item name and status
- User email and phone
- Order user ID and status

### Query Optimization
- Use Specifications for dynamic filtering
- Paginate large result sets
- Enable query caching for frequently accessed data

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 📞 Support

For issues and questions:
- 📧 Email: support@makanforyou.com
- 📖 Documentation: https://docs.makanforyou.com
- 🐛 Issues: https://github.com/makanforyou/backend/issues
- 💬 Discussions: https://github.com/makanforyou/backend/discussions

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- Spring Cloud Gateway for API routing
- MySQL community for database support
- Redis for caching solution
- JWT.io for authentication standards

---

**Made with ❤️ for home cooks and food lovers**
