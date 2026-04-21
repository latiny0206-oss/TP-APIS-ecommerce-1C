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

### Requisitos

- Java 17+
- Maven (o usar el wrapper incluido `./mvnw` / `mvnw.cmd`)
- MySQL 8+ **o** Docker (según la opción elegida)

---

### Opción 1 — H2 en memoria (sin base de datos)

No requiere ninguna instalación externa. La base de datos es volátil: se borra al detener la app. No carga datos semilla.

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La app arranca en `http://localhost:8080`.  
Consola H2: `http://localhost:8080/h2-console`

---

### Opción 2 — MySQL local instalado (perfil `dev`)

Requiere MySQL 8 corriendo en `localhost:3306` con usuario `root` sin contraseña (o ajustar `application-dev.properties`).  
El seed se carga automáticamente al iniciar.

```bash
# Linux / macOS
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run

# Windows (CMD)
set SPRING_PROFILES_ACTIVE=dev && mvnw.cmd spring-boot:run
```

---

### Opción 3 — MySQL via Docker Compose (perfil `mysql`)

El seed se carga automáticamente al iniciar. Primera vez que se ejecuta, Docker también inicializa la base de datos con el script `db/trekking_ecommerce.sql`.

```bash
# 1. Levantar el contenedor MySQL
docker compose up -d

# 2. Iniciar la app (Linux / macOS)
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run

# 2. Iniciar la app (Windows PowerShell)
$env:SPRING_PROFILES_ACTIVE="mysql"; .\mvnw.cmd spring-boot:run

# 2. Iniciar la app (Windows CMD)
set SPRING_PROFILES_ACTIVE=mysql && mvnw.cmd spring-boot:run
```

Credenciales Docker por defecto: `root / 12345`, base de datos `trekking_ecommerce`.

> **Nota:** si la base ya existe (el volumen `mysql_data` no es nuevo), Docker no vuelve a correr el script de init, pero Spring Boot carga el seed igual vía `data.sql` al arrancar.

---

### Opción 4 — MySQL externo con credenciales custom

```bash
# Linux / macOS
SPRING_PROFILES_ACTIVE=mysql \
  DB_HOST=localhost \
  DB_PORT=3306 \
  DB_NAME=trekking_ecommerce \
  DB_USERNAME=tu_usuario \
  DB_PASSWORD=tu_password \
  ./mvnw spring-boot:run
```

---

### Perfiles disponibles

| Perfil | Base de datos | Seed automático | Uso recomendado |
|---|---|---|---|
| `local` (default) | H2 en memoria | No | Desarrollo rápido sin configuración |
| `dev` | MySQL local | Sí | Desarrollo con base de datos real |
| `mysql` | MySQL vía env vars | Sí | Docker / entorno compartido |

---

### Usuarios semilla (perfiles `dev` y `mysql`)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `juanperez` | `user123` | CLIENTE |
| `mariagomez` | `cliente123` | CLIENTE |

Obtener token: `POST /api/auth/login` con `{ "username": "...", "password": "..." }`

---

### JWT Secret

En producción, sobreescribir la clave por defecto con una variable de entorno:

```bash
JWT_SECRET=tu-clave-secreta-larga ./mvnw spring-boot:run
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
