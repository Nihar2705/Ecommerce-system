# E-Commerce Inventory System

A backend-only Spring Boot REST API for managing an e-commerce store's inventory —
Products organized under Categories.

## Tech Stack
- Java 17
- Spring Boot 3.3.0
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- Lombok

## Features
- CRUD operations for Category and Product
- One-to-Many relationship (One Category → Many Products)
- Global exception handling via `@RestControllerAdvice`

## Project Structure
```
src/main/java/com/ecommerce/inventory
│
├── controller     -> REST controllers (CategoryController, ProductController)
├── service        -> Service interfaces + implementations
├── repository     -> Spring Data JPA repositories
├── entity         -> JPA entities (Category, Product)
├── exception      -> ResourceNotFoundException, ErrorResponse, GlobalExceptionHandler
└── EcommerceInventoryApplication.java
```

## Setup

1. Create a MySQL server running locally (or update the connection details).
2. Update `src/main/resources/application.properties` with your MySQL username/password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_inventory_db?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. The API will start on `http://localhost:8080`.

## API Endpoints

### Category
| Method | Endpoint              | Description          |
|--------|-----------------------|-----------------------|
| POST   | `/api/categories`     | Create a category     |
| GET    | `/api/categories`     | Get all categories     |
| GET    | `/api/categories/{id}`| Get category by id     |
| PUT    | `/api/categories/{id}`| Update category        |
| DELETE | `/api/categories/{id}`| Delete category        |

**Sample body:**
```json
{
  "name": "Electronics",
  "description": "Electronic gadgets and devices"
}
```

### Product
| Method | Endpoint            | Description         |
|--------|----------------------|----------------------|
| POST   | `/api/products`      | Create a product     |
| GET    | `/api/products`      | Get all products      |
| GET    | `/api/products/{id}` | Get product by id      |
| PUT    | `/api/products/{id}` | Update product         |
| DELETE | `/api/products/{id}` | Delete product         |

**Sample body:** (category must already exist; only `id` is required in the nested object)
```json
{
  "name": "Wireless Mouse",
  "description": "Bluetooth mouse",
  "price": 799.0,
  "quantity": 50,
  "category": { "id": 1 }
}
```

## Exception Handling
- `ResourceNotFoundException` -> returns HTTP 404 with a structured error body
- Any other unhandled exception -> returns HTTP 500 with a structured error body

Example error response:
```json
{
  "timestamp": "2026-07-05T10:15:30",
  "status": 404,
  "message": "Product not found with id: 99",
  "details": "uri=/api/products/99"
}
```

## Notes
- No DTOs — entities are used directly as request/response bodies.
- `Category.products` is marked `@JsonIgnore` to prevent infinite JSON recursion during serialization.
- No Spring Security, JWT, pagination, sorting, Swagger, Docker, caching, unit tests beyond the
  default context-load test, file upload, logging, or advanced response wrappers were included,
  per project scope.
