# E-Commerce Inventory System

A Spring Boot REST API for managing an e-commerce store's inventory — Products organized
under Categories — now secured with JWT authentication and role-based authorization.

## Version History

**Version 1**
- CRUD for Category and Product
- One-to-Many relationship (Category → Products)
- Global exception handling

**Version 2 adds:**
- Spring Security (stateless, JWT-based)
- Role-based authorization (ADMIN / USER)
- JWT access tokens + database-backed refresh tokens
- A DTO layer so controllers never expose entities directly

**Version 3 (this version) adds:**
- Pagination for `GET /api/categories` and `GET /api/products` using Spring Data's `Pageable`

## Tech Stack
- Java 17
- Spring Boot 3.3.0
- Spring Web, Spring Data JPA, Spring Security
- MySQL
- JWT (jjwt)
- Maven
- Lombok

## Project Structure
```
src/main/java/com/ecommerce/inventory
│
├── controller     -> AuthController, CategoryController, ProductController
├── service        -> Auth/Category/Product service interfaces + implementations
├── repository     -> User, Role, RefreshToken, Category, Product repositories
├── entity         -> User, Role, RefreshToken, Category, Product
├── dto            -> auth/, user/, category/, product/, common/ (PaginatedResponse) DTOs
├── security       -> JwtUtil, JwtAuthenticationFilter, CustomUserDetails(Service),
│                     CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
├── config         -> SecurityConfig, DataInitializer
├── exception      -> ResourceNotFoundException, UserAlreadyExistsException,
│                     InvalidRefreshTokenException, RefreshTokenExpiredException,
│                     ErrorResponse, GlobalExceptionHandler
└── EcommerceInventoryApplication.java
```

## Setup

1. Update `src/main/resources/application.properties` with your MySQL credentials.
2. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. On first startup, `DataInitializer` seeds the `ADMIN` and `USER` roles and creates a
   default admin account:
   - username: `admin`
   - password: `Admin@123`

   (Self-registration via `/api/auth/register` always creates a `USER` account — there is
   no way to register as ADMIN through the API, by design.)

## Authentication Flow

1. **Register** a new user (always created with role `USER`):
   ```
   POST /api/auth/register
   { "username": "john", "email": "john@example.com", "password": "Passw0rd!" }
   ```

2. **Login** with username or email:
   ```
   POST /api/auth/login
   { "usernameOrEmail": "john", "password": "Passw0rd!" }
   ```
   Response:
   ```json
   {
     "accessToken": "eyJhbGciOi...",
     "refreshToken": "b3f1c9a0-...",
     "tokenType": "Bearer",
     "user": { "id": 2, "username": "john", "email": "john@example.com", "role": "USER" }
   }
   ```

3. **Call protected endpoints** with the access token:
   ```
   Authorization: Bearer eyJhbGciOi...
   ```

4. **Refresh** the access token once it expires:
   ```
   POST /api/auth/refresh-token
   { "refreshToken": "b3f1c9a0-..." }
   ```
   Returns a new access token. If the refresh token is unknown or expired, a 401 error is
   returned instead.

## Authorization Rules

| Action                        | ADMIN | USER |
|--------------------------------|-------|------|
| View Categories / Products     | ✅    | ✅   |
| Create/Update/Delete Category  | ✅    | ❌   |
| Create/Update/Delete Product   | ✅    | ❌   |

Enforced both at the URL level (`SecurityConfig`) and at the method level
(`@PreAuthorize` on controller methods).

## API Endpoints

### Auth
| Method | Endpoint                     | Access |
|--------|-------------------------------|--------|
| POST   | `/api/auth/register`         | Public |
| POST   | `/api/auth/login`            | Public |
| POST   | `/api/auth/refresh-token`    | Public |

### Category
| Method | Endpoint               | Access        |
|--------|-------------------------|---------------|
| POST   | `/api/categories`      | ADMIN         |
| GET    | `/api/categories?page=&size=` | ADMIN, USER   |
| GET    | `/api/categories/{id}` | ADMIN, USER   |
| PUT    | `/api/categories/{id}` | ADMIN         |
| DELETE | `/api/categories/{id}` | ADMIN         |

### Product
| Method | Endpoint             | Access        |
|--------|-----------------------|---------------|
| POST   | `/api/products`      | ADMIN         |
| GET    | `/api/products?page=&size=` | ADMIN, USER   |
| GET    | `/api/products/{id}` | ADMIN, USER   |
| PUT    | `/api/products/{id}` | ADMIN         |
| DELETE | `/api/products/{id}` | ADMIN         |

All Category/Product request and response bodies use DTOs
(`CategoryRequest`/`CategoryResponse`, `ProductRequest`/`ProductResponse`) — entities are
never exposed. `ProductRequest` references a category via `categoryId`.

## Pagination (Version 3)

`GET /api/categories` and `GET /api/products` accept optional `page` and `size` query
parameters:

```
GET /api/categories?page=0&size=5
GET /api/products?page=0&size=10
```

- `page` defaults to `0` if not provided.
- `size` defaults to `5` for categories and `10` for products if not provided.
- Both endpoints return a `PaginatedResponse<T>`:

```json
{
  "content": [ { "id": 1, "name": "Electronics", "description": "..." } ],
  "pageNumber": 0,
  "pageSize": 5,
  "totalElements": 12,
  "totalPages": 3,
  "last": false
}
```

Pagination is available to both ADMIN and USER roles, matching the existing GET access
rules. No sorting, searching, or filtering was added.

## Exception Handling

`GlobalExceptionHandler` keeps all Version 1 handling and adds:

| Scenario                         | Exception                        | HTTP Status |
|-----------------------------------|-----------------------------------|--------------|
| Resource not found (V1)          | `ResourceNotFoundException`      | 404          |
| Invalid login credentials        | `BadCredentialsException`        | 401          |
| Duplicate username/email          | `UserAlreadyExistsException`     | 409          |
| Unknown refresh token            | `InvalidRefreshTokenException`   | 401          |
| Expired refresh token            | `RefreshTokenExpiredException`   | 401          |
| Not authenticated                | `AuthenticationException`        | 401          |
| Authenticated but not permitted  | `AccessDeniedException`          | 403          |
| Any other unhandled exception (V1) | `Exception`                    | 500          |

Requests rejected directly by the security filter chain (missing/invalid token, or
insufficient role) return the same JSON error shape via
`CustomAuthenticationEntryPoint` (401) and `CustomAccessDeniedHandler` (403).

## Database

New tables added in Version 2 (Category and Product tables are unchanged from Version 1):
- `roles` — id, name (ADMIN/USER)
- `users` — id, username, email, password (BCrypt-hashed), role_id
- `refresh_tokens` — id, token, expiry_date, user_id

## Notes
- No pagination, sorting, Swagger/OpenAPI, Docker, caching, file upload, email
  verification, password reset, OAuth, or logging were added — strictly out of scope
  for this version.
