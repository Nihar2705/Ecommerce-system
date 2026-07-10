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

**Version 3 adds:**
- Pagination for `GET /api/categories` and `GET /api/products` using Spring Data's `Pageable`

**Version 4 adds:**
- Sorting for `GET /api/categories` and `GET /api/products` (`sortBy`, `direction`)
- Filtering for `GET /api/products` (`name`, `category`, `minPrice`, `maxPrice`)

**Version 5 adds:**
- A dedicated search endpoint `GET /api/products/search` supporting keyword, category,
  and price-range search (each used independently, not combined)

**Version 6 (this version) adds:**
- Product image upload/retrieve/replace/delete, stored on the local file system

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
├── service        -> Auth/Category/Product service interfaces + implementations,
│                     FileStorageService(Impl), ProductImageData (V6)
├── repository     -> User, Role, RefreshToken, Category, Product repositories
├── entity         -> User, Role, RefreshToken, Category, Product (+ imageName, V6)
├── dto            -> auth/, user/, category/, product/, common/ (PaginatedResponse) DTOs
├── security       -> JwtUtil, JwtAuthenticationFilter, CustomUserDetails(Service),
│                     CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
├── config         -> SecurityConfig, DataInitializer
├── specification  -> ProductSpecification (dynamic product filtering + search)
├── util           -> PaginationUtils (sort validation/building)
├── exception      -> ResourceNotFoundException, UserAlreadyExistsException,
│                     InvalidRefreshTokenException, RefreshTokenExpiredException,
│                     InvalidSortFieldException, InvalidSearchParameterException,
│                     InvalidPriceRangeException, EmptyFileException (V6),
│                     InvalidFileTypeException (V6), FileSizeExceededException (V6),
│                     FileStorageException (V6), ImageNotFoundException (V6),
│                     ErrorResponse, GlobalExceptionHandler
└── EcommerceInventoryApplication.java

uploads/products    -> local disk storage for uploaded product images (V6)
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
| Upload/Update/Delete Product Image | ✅ | ❌ |
| View Product Image             | ✅    | ✅   |

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
| GET    | `/api/categories?page=&size=&sortBy=&direction=` | ADMIN, USER   |
| GET    | `/api/categories/{id}` | ADMIN, USER   |
| PUT    | `/api/categories/{id}` | ADMIN         |
| DELETE | `/api/categories/{id}` | ADMIN         |

### Product
| Method | Endpoint             | Access        |
|--------|-----------------------|---------------|
| POST   | `/api/products`      | ADMIN         |
| GET    | `/api/products?page=&size=&sortBy=&direction=&name=&category=&minPrice=&maxPrice=` | ADMIN, USER   |
| GET    | `/api/products/{id}` | ADMIN, USER   |
| PUT    | `/api/products/{id}` | ADMIN         |
| DELETE | `/api/products/{id}` | ADMIN         |
| POST   | `/api/products/{id}/image` | ADMIN   |
| GET    | `/api/products/{id}/image`  | ADMIN, USER |
| DELETE | `/api/products/{id}/image`  | ADMIN   |

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

## Sorting & Filtering (Version 4)

**Sorting** — both `GET /api/categories` and `GET /api/products` accept `sortBy` and
`direction`:

```
GET /api/categories?page=0&size=5&sortBy=name&direction=asc
GET /api/products?page=0&size=10&sortBy=price&direction=desc
```

- `sortBy` defaults to `id`, `direction` defaults to `asc` if not provided (any value
  other than `desc`, case-insensitive, is treated as ascending).
- Allowed `sortBy` values:
  - Category: `id`, `name`, `description`
  - Product: `id`, `name`, `description`, `price`, `quantity`, `category` (sorts by the
    related category's name)
- An unrecognized `sortBy` value returns `400 Bad Request` via `InvalidSortFieldException`.

**Filtering** — `GET /api/products` accepts four optional, freely combinable filters:

```
GET /api/products?name=Laptop
GET /api/products?category=Electronics
GET /api/products?minPrice=1000
GET /api/products?maxPrice=5000
GET /api/products?category=Electronics&minPrice=1000&maxPrice=5000
GET /api/products?name=Phone&category=Electronics&page=0&size=10&sortBy=price&direction=asc
```

- `name` — partial, case-insensitive match on product name
- `category` — partial, case-insensitive match on the related category's name
- `minPrice` / `maxPrice` — inclusive price bounds
- Omitted filters are simply not applied; with no filters, all products are returned
- Filtering, pagination, and sorting all compose together in a single query, built with
  the JPA Specification API (`ProductSpecification`)

## Search (Version 5)

`GET /api/products/search` supports three independent search modes. Exactly one must be
used per request — combining them (e.g. `keyword` + `category` in the same call) returns
`400 Bad Request`, as does calling `/search` with none of them:

```
GET /api/products/search?keyword=laptop
GET /api/products/search?category=Electronics
GET /api/products/search?minPrice=1000&maxPrice=5000
```

- **Keyword search** — case-insensitive partial match against product name OR description
- **Category search** — case-insensitive partial match against the related category's name
- **Price range search** — `minPrice` and/or `maxPrice` (inclusive); if both are given and
  `minPrice > maxPrice`, returns `400 Bad Request` via `InvalidPriceRangeException`

All three modes support the existing pagination and sorting parameters:

```
GET /api/products/search?keyword=phone&page=0&size=10&sortBy=price&direction=asc
```

Accessible to both ADMIN and USER, same as the other GET endpoints. The response uses
the same `PaginatedResponse<ProductResponse>` shape as every other paginated endpoint in
this API (field `pageNumber` carries the current page index — the same value referred to
as "currentPage").

## Product Image Upload (Version 6)

Each product can have exactly **one** associated image, stored on the local file system
under the directory configured by `file.upload-dir` (default `uploads/products`) —
only the generated filename is stored in the database, never the binary data or the
full file system path.

**Upload / replace an image:**
```
POST /api/products/{id}/image
Content-Type: multipart/form-data
file: <binary>
```
Uploading a new image for a product that already has one automatically deletes the old
file from disk before saving the new one. Returns the updated `ProductResponse`.

**Retrieve an image:**
```
GET /api/products/{id}/image
```
Streams the raw image bytes with the correct `Content-Type` header (`image/jpeg` or
`image/png`).

**Delete an image:**
```
DELETE /api/products/{id}/image
```
Deletes the file from disk and clears the reference on the product. Returns a plain-text
success message.

**Validation rules:**
- Only `.jpg`, `.jpeg`, and `.png` files are accepted (checked by both content type and
  file extension) — anything else returns `400 Bad Request`
- Maximum file size: **5 MB**, enforced both by Spring's multipart resolver
  (`spring.servlet.multipart.max-file-size`) and explicitly in `FileStorageServiceImpl`
- An empty file (no bytes / nothing selected) returns `400 Bad Request`
- Uploading to, retrieving from, or deleting the image of a non-existent product returns
  `404 Not Found` (the existing `ResourceNotFoundException`)
- Retrieving or deleting the image of a product that has none returns `404 Not Found`
  via the new `ImageNotFoundException`

**DTO changes:** `ProductResponse` now includes `imageName` and `imageUrl` (null when the
product has no image); the file system path is never exposed:
```json
{
  "id": 1,
  "name": "Wireless Mouse",
  "description": "Bluetooth mouse",
  "price": 799.0,
  "quantity": 50,
  "categoryId": 1,
  "categoryName": "Electronics",
  "imageName": "b3f1c9a0-1a2b-4c3d-9e8f-abc123456789.jpg",
  "imageUrl": "/api/products/1/image"
}
```

Access follows the same rules as everything else product-related: ADMIN can
upload/replace/delete, both ADMIN and USER can view.

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
| Invalid sortBy field (V4)        | `InvalidSortFieldException`      | 400          |
| Invalid/combined search params (V5) | `InvalidSearchParameterException` | 400      |
| Invalid price range (V5)         | `InvalidPriceRangeException`     | 400          |
| Empty uploaded file (V6)         | `EmptyFileException`             | 400          |
| Invalid image file type (V6)     | `InvalidFileTypeException`       | 400          |
| Image file too large (V6, app-level check) | `FileSizeExceededException` | 400   |
| Image file too large (V6, servlet-level)  | `MaxUploadSizeExceededException` | 400 |
| Product/image not found on disk (V6) | `ImageNotFoundException`    | 404          |
| File system read/write/delete failure (V6) | `FileStorageException` | 500        |
| Any other unhandled exception (V1) | `Exception`                    | 500          |

Requests rejected directly by the security filter chain (missing/invalid token, or
insufficient role) return the same JSON error shape via
`CustomAuthenticationEntryPoint` (401) and `CustomAccessDeniedHandler` (403).

## Database

New tables added in Version 2 (Category and Product tables are unchanged from Version 1):
- `roles` — id, name (ADMIN/USER)
- `users` — id, username, email, password (BCrypt-hashed), role_id
- `refresh_tokens` — id, token, expiry_date, user_id

Version 6 adds a single new column instead of a new table:
- `products.image_name` — nullable, stores only the generated stored filename

## Notes
- No Swagger/OpenAPI, Docker, caching, file upload, email verification, password reset,
  OAuth, logging, Elasticsearch, or Redis were added — strictly out of scope for this
  version.
- The three product search modes (keyword, category, price range) are mutually
  exclusive per request by design — there is intentionally no combined search endpoint.
- Image storage is local disk only — no cloud storage (S3/Cloudinary/Azure Blob), no
  multiple images per product, and no image compression/resizing.
- Deleting a product that has an image also deletes that image file from disk, to avoid
  leaving orphaned files in `uploads/products` — a small necessary addition alongside the
  requested upload/retrieve/replace/delete behavior.
