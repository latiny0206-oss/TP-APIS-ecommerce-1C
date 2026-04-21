# Trekking E-commerce — Backend API

REST API backend for a trekking/outdoor-gear e-commerce platform, built with Spring Boot 3 and secured with JWT + Role-Based Access Control.

**Grupo 13 — Contartese · Melian · Perrella · Terranova**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security 6 · JWT (jjwt 0.12) · BCrypt |
| Persistence | Spring Data JPA · Hibernate · MySQL 8 |
| Documentation | SpringDoc OpenAPI 2 (Swagger UI) |
| Local dev DB | H2 in-memory |
| Containerization | Docker Compose |
| Build | Maven Wrapper |

---

## Key Features

- **JWT Authentication** — stateless token-based auth with 24-hour expiry
- **Role-Based Access Control** — `ADMIN` and `USER` roles enforced via `@PreAuthorize`
- **Full CRUD** for all 11 domain entities
- **Shopping Cart logic** — add/remove items, apply discounts, checkout to Orden
- **Scheduled Jobs** — automatic cleanup of abandoned carts (Mondays 02:00) and expiry of outdated discounts (daily 00:05)
- **Global Exception Handling** — structured `ErrorResponse` for all error cases
- **Multi-profile configuration** — H2 for local dev, MySQL for production

---

## Domain Entities (DER)

```
Usuario ──< Carrito ──< ItemCarrito >── VarianteProducto >── Producto >── Categoria
                                                                        >── Marca
                                                                        └──< Foto
Carrito >── Descuento
Usuario ──< Orden ──< ItemOrden >── VarianteProducto
```

| Entity | Description |
|---|---|
| `Usuario` | Platform user with role (ADMIN / USER) |
| `Producto` | Product with category, brand, status and variants |
| `VarianteProducto` | SKU-level variant (size, color, price, stock) |
| `Categoria` | Product category |
| `Marca` | Brand |
| `Foto` | Product image URLs |
| `Carrito` | Shopping cart (VACIO / ACTIVO / ABANDONADO / COMPRADO) |
| `ItemCarrito` | Line item inside a cart |
| `Orden` | Purchase order generated on checkout |
| `ItemOrden` | Snapshot of item at purchase time |
| `Descuento` | Discount (fixed or percentage, with validity dates) |

---

## Project Structure

```
src/main/java/com/trekking/ecommerce/
├── config/          # Spring Security config, OpenAPI/Swagger config
├── controller/      # REST controllers (one per entity + AuthController)
├── dto/             # Request/Response DTOs (no entities exposed directly)
├── exception/       # GlobalExceptionHandler, BusinessRuleException, ResourceNotFoundException
├── job/             # @Scheduled jobs (CarritoJob, DescuentoJob)
├── model/           # @Entity classes + enums
│   └── enums/
├── repository/      # Spring Data JPA repositories
├── security/        # JwtUtil, JwtAuthenticationFilter, UserDetailsServiceImpl
└── service/         # Service interfaces + impl/ implementations
```

---

## Setup & Running

### Prerequisites

- Java 17+
- Maven (or use the included `./mvnw` wrapper)
- Docker (optional, for MySQL)

---

### Option 1 — Local dev with H2 (no database required)

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with an H2 in-memory database.  
H2 console available at: `http://localhost:8080/h2-console`

---

### Option 2 — MySQL via Docker Compose

```bash
# Start MySQL container
docker compose up -d

# Run the app pointing at MySQL
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
```

Default Docker MySQL credentials: `root / 12345`, database `trekking_ecommerce`.

---

### Option 3 — External MySQL (custom credentials)

```bash
SPRING_PROFILES_ACTIVE=mysql \
  DB_HOST=localhost \
  DB_PORT=3306 \
  DB_NAME=trekking_ecommerce \
  DB_USERNAME=your_user \
  DB_PASSWORD=your_password \
  ./mvnw spring-boot:run
```

---

### Environment Profiles

| Profile | Database | Use case |
|---|---|---|
| `local` (default) | H2 in-memory | Local development, no setup needed |
| `dev` | MySQL local | Development with real DB |
| `mysql` | MySQL (env vars) | Production / Docker |
| `test` | H2 in-memory | Automated tests |

---

### JWT Secret

Override the default secret via environment variable (required in production):

```bash
JWT_SECRET=your-very-long-secret-key-here ./mvnw spring-boot:run
```

---

## API Documentation

Swagger UI is available once the app is running:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/api-docs
```

All protected endpoints require a `Bearer <token>` header. Use `POST /api/auth/login` to obtain a token.

---

## Authentication

| Endpoint | Method | Access | Description |
|---|---|---|---|
| `/api/auth/register` | POST | Public | Register a new user |
| `/api/auth/login` | POST | Public | Obtain JWT token |
| `/api/auth/me` | GET | Authenticated | Current user info |

---

## Access Control Summary

| Resource | GET | POST / PUT / DELETE |
|---|---|---|
| Productos, Categorias, Marcas, Variantes, Fotos | Public | ADMIN only |
| Descuentos (activos) | Authenticated | ADMIN only |
| Usuarios | — | ADMIN only |
| Carritos, Órdenes | Authenticated (own) | Authenticated |

---

## Scheduled Jobs

| Job | Schedule | Action |
|---|---|---|
| `CarritoJob` | Every Monday at 02:00 | Marks carts inactive for 7+ days as `ABANDONADO` and clears items |
| `DescuentoJob` | Daily at 00:05 | Marks expired `ACTIVO` discounts as `EXPIRADO` |
