postman collection - https://yevgeniikurlykov.postman.co/workspace/05afd32d-6a65-4f53-bb4a-1be9a2931d42


# My project — Spring Boot E-Commerce API

## Project Overview

**My project** is a backend REST API for an e-commerce platform built with **Spring Boot**.  
The project implements a complete online store backend with role-based access control and covers the full business flow:

- user registration and authentication
- role-based authorization (ADMIN / SELLER / BUYER)
- product and category management
- buyer cart management
- order creation and processing
- inventory (stock) management
- database migrations with Liquibase
- unit and integration testing using JUnit

Authentication is implemented using **Spring Security with HTTP Basic Authentication**  
(no JWT, classic server-side security model).

---

## Architecture

The application follows a layered architecture:

```

Controller → Service → Repository → Database
Mapper → DTO

```

Key principles:
- Controllers contain no business logic
- Services encapsulate domain rules
- Entities are never returned directly (DTOs are used)
- Mapping between entities and DTOs is handled by MapStruct

---

## Project Structure

```

src/main/java/org/example/session
│
├── controllers
│   ├── auth
│   ├── category
│   ├── product
│   ├── inventory
│   ├── order
│   └── CartBuyerController
│
├── service
│   ├── AuthService
│   ├── ProductService
│   ├── CategoryService
│   ├── CartService
│   ├── OrderService
│   └── InventoryService
│
├── db
│   ├── entity
│   │   ├── User
│   │   ├── Role
│   │   ├── Product
│   │   ├── Category
│   │   ├── Inventory
│   │   ├── Cart
│   │   ├── CartItem
│   │   ├── Order
│   │   └── OrderItem
│   │
│   └── repository
│
├── data
│   ├── dtos
│   │   ├── request
│   │   └── response
│   └── mappers
│
├── security
│   ├── User (UserDetails, UserDetailsService)
│   └── SecurityConfig
│
└── SessionApplication

```

---

## Database

- PostgreSQL
- Schema management via **Liquibase**
- All database changes are applied through changelog files

Main tables:
- `users`
- `roles`
- `products`
- `categories`
- `inventory`
- `carts`
- `cart_items`
- `orders`
- `order_items`

---

## User Roles

| Role   | Description |
|------|------------|
| ADMIN | Full system access |
| SELLER | Manages own products and inventory |
| BUYER | Uses cart and places orders |

---

## Security & Authentication

- Spring Security
- HTTP Basic Authentication
- `AuthenticationManager`
- `UserDetailsService`
- Role-based access control

---

## REST API

### Public (No Authentication)

#### Registration

```

POST /api/public/buyer/register
POST /api/public/seller/register

````

Request body:
```json
{
  "email": "user@mail.com",
  "password": "123456",
  "fullName": "John Doe"
}
````

---

### Authentication

```
POST   /api/auth/login
GET    /api/auth/me
PUT    /api/auth/profile
PATCH  /api/auth/change-password
```

---

### Categories

#### Public

```
GET /api/categories
GET /api/categories/{id}
GET /api/categories/{id}/children
```

#### Admin

```
POST /api/admin/categories
```

---

### Products

#### Public

```
GET /api/products
GET /api/products/{id}
```

#### Seller

```
POST   /api/seller/products
GET    /api/seller/products
PUT    /api/seller/products/{id}
DELETE /api/seller/products/{id}
```

#### Admin

```
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
```

---

### Inventory

#### Public

```
GET /api/inventory/products/{productId}
```

#### Seller

```
PUT /api/seller/inventory/products/{productId}
```

---

### Cart (Buyer)

```
GET    /api/buyer/cart
POST   /api/buyer/cart/items
PUT    /api/buyer/cart/items
DELETE /api/buyer/cart/items/{productId}
DELETE /api/buyer/cart
```

---

### Orders

#### Buyer

```
POST /api/buyer/orders/checkout
GET  /api/buyer/orders
GET  /api/buyer/orders/{id}
```

#### Admin

```
PUT /api/admin/orders/{id}/status
```

---

## Testing

* JUnit 5
* No Mockito (real services and repositories)
* Separate `application-test.yml`
* Dedicated PostgreSQL test database

```
src/test/java
├── services
├── mappers
└── SessionApplicationTests
```

---

## Running the Project

### Start PostgreSQL with Docker

```bash
docker compose up -d
```

### Run the Application

```bash
./gradlew bootRun
```

---

## Technologies Used

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* Liquibase
* MapStruct
* JUnit 5
* Docker

---

## Summary

This project demonstrates:

* clean Spring Boot architecture
* proper separation of concerns
* role-based security
* full e-commerce business flow
* database migration best practices
* production-ready backend structure
