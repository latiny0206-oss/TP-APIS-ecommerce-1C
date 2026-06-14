# DOCUMENTACIÓN DE INTEGRACIÓN BACKEND-FRONTEND
## Trekking Ecommerce - Guía Completa para el Equipo Frontend

---

## RESUMEN EJECUTIVO

Este documento fue generado mediante análisis exhaustivo del proyecto Spring Boot **Trekking Ecommerce**. Cubre todos los endpoints REST, modelos de datos, configuración de seguridad JWT, flujos de negocio y ejemplos completos de integración en React.

### Stack Técnico
| Componente | Tecnología |
|-----------|-----------|
| Backend | Java 17 + Spring Boot 3.5.12 |
| Autenticación | JWT (JJWT 0.12.3) - Stateless |
| Base de Datos | MySQL (producción) / H2 (testing) |
| ORM | Spring Data JPA + Hibernate |
| Documentación API | Swagger UI / OpenAPI 3 |
| Validación | Bean Validation (Jakarta) |
| Seguridad | Spring Security + BCrypt |
| Upload de Archivos | Spring Multipart (max 10MB/archivo, 30MB/request) |

### URLs Importantes
```
Base URL:         http://localhost:8080/api
Swagger UI:       http://localhost:8080/swagger-ui.html
API Docs JSON:    http://localhost:8080/api-docs
OpenAPI v3:       http://localhost:8080/v3/api-docs
```

---

## ÍNDICE

1. [Arquitectura del Backend](#1-arquitectura-del-backend)
2. [Autenticación y Seguridad](#2-autenticación-y-seguridad)
3. [Endpoints Completos](#3-endpoints-completos)
4. [Modelos de Datos](#4-modelos-de-datos)
5. [Enumeraciones](#5-enumeraciones)
6. [Guía de Integración React](#6-guía-de-integración-react)
7. [Manejo de Errores](#7-manejo-de-errores)
8. [Flujos de Negocio Clave](#8-flujos-de-negocio-clave)
9. [Troubleshooting](#9-troubleshooting)
10. [Preguntas Frecuentes](#10-preguntas-frecuentes)

---

## 1. ARQUITECTURA DEL BACKEND

### Estructura de Paquetes
```
com.trekking.ecommerce/
├── config/           → Configuración de seguridad, CORS, OpenAPI
├── controller/       → REST Controllers (endpoints HTTP)
├── dto/              → Data Transfer Objects (Request/Response)
├── exception/        → Excepciones personalizadas y GlobalExceptionHandler
├── job/              → Tareas programadas (DescuentoJob, CarritoJob)
├── model/            → Entidades JPA
│   └── enums/        → Enumeraciones de dominio
├── repository/       → Interfaces Spring Data JPA
├── security/         → JWT Filter, UserDetailsService, utilidades JWT
└── service/          → Interfaces + implementaciones de negocio
    └── impl/
```

### Patrón de Respuesta
- **Éxito**: HTTP 200/201/204 con cuerpo JSON según endpoint
- **Error**: HTTP 4xx/5xx con body `ErrorResponse`:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error"
}
```

### Tareas Automáticas (Background Jobs)
| Job | Horario | Función |
|-----|---------|---------|
| DescuentoJob | Diariamente 00:05 | Marca como EXPIRADO los descuentos vencidos |
| CarritoJob | Lunes 02:00 | Marca ABANDONADO carritos sin actividad > 7 días |

---

## 2. AUTENTICACIÓN Y SEGURIDAD

### Tipo de Autenticación
**JWT Bearer Token** - Stateless (sin sesión en servidor)
- Duración del token: **24 horas** (86400000 ms)
- Algoritmo: HMAC-SHA (256)

### Flujo de Login
```
1. POST /api/auth/login  →  { username, password }
2. Respuesta:            →  { token, username, rol }
3. Guardar token         →  localStorage.setItem('token', token)
4. Usar en requests      →  Authorization: Bearer <token>
```

### Roles y Permisos
| Rol | Valor en JWT | Descripción |
|-----|-------------|-------------|
| `ROLE_ADMIN` | `ADMIN` | Acceso total a todos los endpoints |
| `ROLE_CLIENTE` | `CLIENTE` | Acceso a su propio carrito y órdenes |

### Endpoints Públicos (sin token)
```
POST   /api/auth/login
POST   /api/auth/register
GET    /api/productos/**
GET    /api/categorias/**
GET    /api/marcas/**
GET    /api/variantes/**
GET    /api/fotos/**
GET    /swagger-ui.html
GET    /api-docs
```

### Endpoints que Requieren Autenticación
```
GET    /api/descuentos/activos     → Cualquier usuario autenticado
/api/carritos/**                   → Usuario autenticado (dueño o admin)
/api/ordenes/**                    → Usuario autenticado (dueño o admin)
```

### Endpoints Solo Admin
```
POST/PUT/DELETE  /api/productos/**
POST/PUT/DELETE  /api/categorias/**
POST/PUT/DELETE  /api/marcas/**
POST/PUT/DELETE  /api/variantes/**
POST/PUT/DELETE  /api/fotos/**
ALL              /api/usuarios/**
ALL              /api/descuentos/**  (excepto GET /activos)
POST             /api/ordenes        (crear orden manual)
PUT              /api/ordenes/{id}
```

### Configuración CORS
El backend permite actualmente:
- **Orígenes**: `*` (todos)
- **Métodos**: GET, POST, PUT, DELETE, OPTIONS
- **Headers**: todos
- **Credentials**: habilitado

---

## 3. ENDPOINTS COMPLETOS

### 3.1 Autenticación (`/api/auth`)

#### POST /api/auth/login
```
Autenticación: No requerida
Content-Type:  application/json
```
**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```
**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "juan_perez",
  "rol": "CLIENTE"
}
```
**Errores:**
- `401` - Usuario o contraseña incorrectos
- `403` - La cuenta está inactiva

---

#### POST /api/auth/register
```
Autenticación: No requerida
Content-Type:  application/json
```
**Request:**
```json
{
  "username": "juan_perez",
  "email": "juan@email.com",
  "password": "Password1",
  "nombre": "Juan",
  "apellido": "Pérez"
}
```
> Contraseña: mínimo 8 caracteres, 1 mayúscula, 1 dígito

**Response 201 Created:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "juan_perez",
  "rol": "CLIENTE"
}
```
**Errores:**
- `400` - Validaciones fallidas o usuario/email ya existe

---

### 3.2 Usuarios (`/api/usuarios`) — Solo ADMIN

#### GET /api/usuarios
```
Autenticación: ADMIN
```
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@trekking.com",
    "nombre": "Admin",
    "apellido": "Principal",
    "rol": "ADMIN",
    "estado": "ACTIVO"
  }
]
```

#### GET /api/usuarios/{id}
**Response 200 OK:** `UsuarioResponse` (objeto único)
**Errores:** `404` - Usuario no encontrado

#### POST /api/usuarios
**Request:**
```json
{
  "username": "nuevo_usuario",
  "email": "nuevo@email.com",
  "password": "Password1",
  "nombre": "Nuevo",
  "apellido": "Usuario",
  "rol": "CLIENTE",
  "estado": "ACTIVO"
}
```
**Response:** `201 Created` con `UsuarioResponse`
**Errores:** `400` validaciones, `409` username/email duplicado

#### PUT /api/usuarios/{id}
**Request:** mismo que POST (password opcional en actualización)
**Response:** `200 OK` con `UsuarioResponse`

#### DELETE /api/usuarios/{id}
**Response:** `204 No Content`

---

### 3.3 Categorías (`/api/categorias`)

#### GET /api/categorias — Público
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "nombre": "Mochilas",
    "descripcion": "Mochilas de trekking y senderismo"
  }
]
```

#### GET /api/categorias/{id} — Público
**Response 200 OK:** `CategoriaResponse`
**Errores:** `404`

#### POST /api/categorias — ADMIN
**Request:**
```json
{
  "nombre": "Calzado",
  "descripcion": "Botas y zapatillas de montaña"
}
```
**Response:** `201 Created`
**Errores:** `400` nombre ya existe

#### PUT /api/categorias/{id} — ADMIN
**Request:** mismo que POST
**Response:** `200 OK`

#### DELETE /api/categorias/{id} — ADMIN
**Response:** `204 No Content`

---

### 3.4 Marcas (`/api/marcas`)

Misma estructura que categorías.

**GET /api/marcas — Público:**
```json
[
  {
    "id": 1,
    "nombre": "Salomon",
    "descripcion": "Marca francesa de equipamiento outdoor"
  }
]
```

**POST/PUT /api/marcas — ADMIN:**
```json
{
  "nombre": "The North Face",
  "descripcion": "Marca americana de montañismo"
}
```

---

### 3.5 Productos (`/api/productos`)

#### GET /api/productos — Público
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "marcaId": 2,
    "marcaNombre": "Salomon",
    "categoriaId": 1,
    "categoriaNombre": "Calzado",
    "nombre": "X Ultra 4 GTX",
    "descripcion": "Bota de senderismo impermeable Gore-Tex",
    "estado": "ACTIVO",
    "precioBase": 189.99
  }
]
```

#### GET /api/productos/{id} — Público
**Response 200 OK:** `ProductoResponse` único

#### GET /api/productos/categoria/{categoriaId} — Público
**Response 200 OK:** `List<ProductoResponse>` filtrada por categoría

#### GET /api/productos/marca/{marcaId} — Público
**Response 200 OK:** `List<ProductoResponse>` filtrada por marca

#### GET /api/productos/estado/{estado} — Público
**Path:** estado = `ACTIVO` | `PAUSADO` | `ELIMINADO`
**Response 200 OK:** `List<ProductoResponse>`

#### GET /api/productos/{id}/disponible — Público
**Response 200 OK:** `true` o `false`
> `true` si estado=ACTIVO Y al menos una variante tiene stock > 0

#### POST /api/productos — ADMIN
**Request:**
```json
{
  "marcaId": 2,
  "categoriaId": 1,
  "nombre": "Speedcross 6",
  "descripcion": "Zapatilla trail running",
  "estado": "ACTIVO",
  "precioBase": 149.99
}
```
**Response:** `201 Created` con `ProductoResponse`
**Errores:** `400` validaciones, `404` marca/categoría no existe, `409` nombre+marca+categoría duplicado

#### PUT /api/productos/{id} — ADMIN
**Request:** mismo que POST
**Response:** `200 OK`

#### DELETE /api/productos/{id} — ADMIN
**Response:** `204 No Content`

---

### 3.6 Variantes de Producto (`/api/variantes`)

#### GET /api/variantes — Público
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "productoId": 1,
    "productoNombre": "X Ultra 4 GTX",
    "color": "Negro/Gris",
    "talla": "42",
    "material": "Gore-Tex + cuero",
    "peso": 0.850,
    "stock": 15,
    "precio": 189.99,
    "estacion": "INVIERNO"
  }
]
```

#### GET /api/variantes/{id} — Público
**Response 200 OK:** `VarianteProductoResponse` único

#### GET /api/variantes/{id}/precio — Público
**Response 200 OK:** `189.99` (BigDecimal)

#### GET /api/variantes/{id}/stock/disponible?cantidad={n} — Público
**Query param:** `cantidad` (entero >= 1)
**Response 200 OK:** `true` o `false`

#### POST /api/variantes — ADMIN
**Request:**
```json
{
  "productoId": 1,
  "color": "Azul/Negro",
  "talla": "43",
  "material": "Gore-Tex",
  "peso": 0.900,
  "stock": 10,
  "precio": 189.99,
  "estacion": "INVIERNO"
}
```
**Response:** `201 Created`
**Errores:** `404` producto no existe, `409` combinación color+talla duplicada para ese producto

#### PUT /api/variantes/{id} — ADMIN
**Request:** mismo que POST

#### DELETE /api/variantes/{id} — ADMIN
**Response:** `204 No Content`
**Errores:** `400` si la variante está referenciada en un carrito activo u orden

---

### 3.7 Fotos (`/api/fotos`)

#### GET /api/fotos — Público
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "varianteId": 1,
    "nombre": "bota-negro-frontal.jpg",
    "tipoContenido": "image/jpeg",
    "orden": 1,
    "datos": "base64EncodedString..."
  }
]
```
> `datos` viene como string Base64. Para mostrar: `<img src={`data:${foto.tipoContenido};base64,${foto.datos}`} />`

#### GET /api/fotos/{id} — Público
**Response 200 OK:** `FotoResponse` único

#### GET /api/fotos/variante/{varianteId} — Público
**Response 200 OK:** `List<FotoResponse>` de fotos de esa variante

#### POST /api/fotos — ADMIN
```
Content-Type: multipart/form-data
```
**Form Parameters:**
- `varianteId` (Long, requerido)
- `orden` (Integer, requerido)
- `archivo` (MultipartFile, requerido, max 10MB)

**Response:** `201 Created` con `FotoResponse`
**Errores:** `400` archivo inválido/vacío, `404` variante no existe, `413` archivo muy grande, `415` usar multipart/form-data

#### PUT /api/fotos/{id} — ADMIN
**Form:** mismo que POST
**Response:** `200 OK`

#### DELETE /api/fotos/{id} — ADMIN
**Response:** `204 No Content`

---

### 3.8 Descuentos (`/api/descuentos`)

#### GET /api/descuentos — Público (lista todos)
**Response 200 OK:**
```json
[
  {
    "id": 1,
    "nombre": "VERANO2024",
    "tipo": "PORCENTAJE",
    "valor": 15.00,
    "fechaInicio": "2024-12-01",
    "fechaFin": "2024-12-31",
    "estado": "ACTIVO"
  }
]
```

#### GET /api/descuentos/activos — Autenticado
**Response 200 OK:** `List<DescuentoResponse>` solo con `estado=ACTIVO`

#### GET /api/descuentos/{id} — Público
**Response 200 OK:** `DescuentoResponse` único

#### GET /api/descuentos/{id}/vigente — Público
**Response 200 OK:** `true` o `false`
> `true` si estado=ACTIVO Y hoy está entre fechaInicio y fechaFin

#### GET /api/descuentos/{id}/calcular?monto={valor} — Público
**Query param:** `monto` (BigDecimal >= 0)
**Response 200 OK:** monto del descuento a aplicar (BigDecimal)
> Para FIJO: min(valor, monto). Para PORCENTAJE: monto * valor / 100

#### POST /api/descuentos — ADMIN
**Request:**
```json
{
  "nombre": "NAVIDAD2024",
  "tipo": "PORCENTAJE",
  "valor": 20.00,
  "fechaInicio": "2024-12-20",
  "fechaFin": "2024-12-26",
  "estado": "ACTIVO"
}
```
> Para tipo FIJO: `"valor": 500.00` (monto absoluto)
> Para tipo PORCENTAJE: `"valor": 15.00` (porcentaje, 0 < valor <= 100)

**Response:** `201 Created`
**Errores:** `400` nombre duplicado, fechas inválidas, valor inválido

#### PUT /api/descuentos/{id} — ADMIN
**Request:** mismo que POST

#### DELETE /api/descuentos/{id} — ADMIN
**Response:** `204 No Content`
**Errores:** `400` si descuento está aplicado en un carrito activo

---

### 3.9 Carritos (`/api/carritos`) — Autenticado

#### GET /api/carritos — Autenticado
> Admin ve todos. Cliente ve solo los suyos.

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "usuarioId": 5,
    "usuarioUsername": "juan_perez",
    "descuentoId": null,
    "estado": "ACTIVO",
    "montoTotal": 379.98,
    "fechaUltimaModificacion": "2024-01-15T14:30:00",
    "items": [
      {
        "id": 1,
        "varianteId": 3,
        "varianteColor": "Negro/Gris",
        "varianteTalla": "42",
        "productoNombre": "X Ultra 4 GTX",
        "cantidad": 2,
        "precioUnitario": 189.99
      }
    ]
  }
]
```

#### GET /api/carritos/{id} — Autenticado (dueño o admin)
**Response 200 OK:** `CarritoResponse` único
**Errores:** `403` no es dueño, `404` no existe

#### POST /api/carritos — Autenticado
**Request:**
```json
{
  "usuarioId": 5,
  "descuentoId": null
}
```
> Un usuario solo puede tener un carrito ACTIVO o VACIO a la vez.

**Response:** `201 Created` con `CarritoResponse`
**Errores:** `400` usuario ya tiene carrito activo

#### PUT /api/carritos/{id} — Autenticado
**Request:** mismo que POST (para actualizar descuento aplicado)
**Response:** `200 OK`

#### DELETE /api/carritos/{id} — Autenticado
**Response:** `204 No Content`

#### POST /api/carritos/{id}/items — Agregar ítem
**Request:**
```json
{
  "idVariante": 3,
  "cantidad": 2
}
```
> Si el ítem ya existe, actualiza la cantidad (no duplica).

**Response 200 OK:** `ItemCarritoResponse`
```json
{
  "id": 1,
  "varianteId": 3,
  "varianteColor": "Negro/Gris",
  "varianteTalla": "42",
  "productoNombre": "X Ultra 4 GTX",
  "cantidad": 2,
  "precioUnitario": 189.99
}
```
**Errores:** `400` carrito CONVERTIDO/ABANDONADO, producto no ACTIVO

#### DELETE /api/carritos/{id}/items/{idItem} — Eliminar ítem
**Response:** `204 No Content`

#### PUT /api/carritos/{id}/items/{idItem}?cantidad={n} — Actualizar cantidad
**Query param:** `cantidad` (entero >= 1)
**Response 200 OK:** `ItemCarritoResponse`

#### GET /api/carritos/{id}/items — Listar ítems
**Response 200 OK:** `List<ItemCarritoResponse>`

#### GET /api/carritos/{id}/total — Total con descuento
**Response 200 OK:** `379.98` (BigDecimal)
> Calcula: suma de ítems, aplica descuento si vigente, retorna max(total, 0)

#### POST /api/carritos/{id}/vaciar — Vaciar carrito
**Response:** `204 No Content`
> Elimina todos los ítems, pone montoTotal=0, estado=VACIO

#### POST /api/carritos/{id}/checkout — Realizar compra
**Response 200 OK:** `OrdenResponse`
```json
{
  "id": 1,
  "usuarioId": 5,
  "carritoId": 1,
  "descuentoId": null,
  "fechaCreacion": "2024-01-15T15:00:00",
  "montoFinal": 379.98,
  "estado": "PENDIENTE",
  "items": [
    {
      "id": 1,
      "varianteId": 3,
      "varianteColor": "Negro/Gris",
      "varianteTalla": "42",
      "productoNombre": "X Ultra 4 GTX",
      "cantidad": 2,
      "precioAlMomento": 189.99
    }
  ]
}
```
> Descuenta stock, crea orden PENDIENTE, marca carrito CONVERTIDO.
**Errores:** `400` carrito vacío

---

### 3.10 Órdenes (`/api/ordenes`) — Autenticado

#### GET /api/ordenes — Autenticado
> Admin ve todas. Cliente ve solo las suyas.

**Response 200 OK:** `List<OrdenResponse>`

#### GET /api/ordenes/{id} — Autenticado (dueño o admin)
**Response 200 OK:** `OrdenResponse`

#### GET /api/ordenes/usuario/{idUsuario} — Autenticado
> Cliente solo puede ver sus propias órdenes.

**Response 200 OK:** `List<OrdenResponse>`

#### GET /api/ordenes/{id}/items — Autenticado
**Response 200 OK:** `List<ItemOrdenResponse>`

#### GET /api/ordenes/{id}/monto-final — Autenticado
**Response 200 OK:** `379.98` (BigDecimal)

#### POST /api/ordenes — Solo ADMIN (crear orden manual)
**Request:**
```json
{
  "usuarioId": 5,
  "carritoId": null,
  "descuentoId": null,
  "fechaCreacion": "2024-01-15T15:00:00",
  "montoFinal": 379.98,
  "estado": "PENDIENTE"
}
```
**Response:** `201 Created`

#### PUT /api/ordenes/{id} — Solo ADMIN
**Request:** mismo que POST
**Response:** `200 OK`

#### DELETE /api/ordenes/{id} — Autenticado (dueño o admin)
**Response:** `204 No Content`
> Si la orden no está ENTREGADA ni CANCELADA, restaura el stock.

#### POST /api/ordenes/{id}/confirmar — Autenticado (dueño o admin)
> Cambia estado de PENDIENTE a CONFIRMADA.

**Response 200 OK:** `OrdenResponse`
**Errores:** `400` si no está en estado PENDIENTE

#### POST /api/ordenes/{id}/cancelar — Autenticado (dueño o admin)
> Restaura stock de todos los ítems. Cambia a CANCELADA.

**Response 200 OK:** `OrdenResponse`
**Errores:** `400` si ya está ENTREGADA o CANCELADA

---

## 4. MODELOS DE DATOS

### AuthResponse
```typescript
interface AuthResponse {
  token: string;
  username: string;
  rol: 'ADMIN' | 'CLIENTE';
}
```

### UsuarioResponse
```typescript
interface UsuarioResponse {
  id: number;
  username: string;
  email: string;
  nombre: string;
  apellido: string;
  rol: 'ADMIN' | 'CLIENTE';
  estado: 'ACTIVO' | 'INACTIVO';
}
```

### ProductoResponse
```typescript
interface ProductoResponse {
  id: number;
  marcaId: number;
  marcaNombre: string;
  categoriaId: number;
  categoriaNombre: string;
  nombre: string;
  descripcion: string | null;
  estado: 'ACTIVO' | 'PAUSADO' | 'ELIMINADO';
  precioBase: number;
}
```

### VarianteProductoResponse
```typescript
interface VarianteProductoResponse {
  id: number;
  productoId: number;
  productoNombre: string;
  color: string;
  talla: string;
  material: string;
  peso: number;
  stock: number;
  precio: number;
  estacion: 'PRIMAVERA' | 'VERANO' | 'OTONO' | 'INVIERNO';
}
```

### CategoriaResponse / MarcaResponse
```typescript
interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
}
// MarcaResponse tiene la misma estructura
```

### FotoResponse
```typescript
interface FotoResponse {
  id: number;
  varianteId: number;
  nombre: string;
  tipoContenido: string; // "image/jpeg", "image/png", etc.
  orden: number;
  datos: string; // Base64 encoded - usar: `data:${tipoContenido};base64,${datos}`
}
```

### DescuentoResponse
```typescript
interface DescuentoResponse {
  id: number;
  nombre: string;
  tipo: 'PORCENTAJE' | 'FIJO';
  valor: number;
  fechaInicio: string; // "YYYY-MM-DD"
  fechaFin: string;    // "YYYY-MM-DD"
  estado: 'ACTIVO' | 'EXPIRADO';
}
```

### CarritoResponse
```typescript
interface ItemCarritoResponse {
  id: number;
  varianteId: number;
  varianteColor: string;
  varianteTalla: string;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
}

interface CarritoResponse {
  id: number;
  usuarioId: number;
  usuarioUsername: string;
  descuentoId: number | null;
  estado: 'ACTIVO' | 'VACIO' | 'ABANDONADO' | 'CONVERTIDO';
  montoTotal: number;
  fechaUltimaModificacion: string; // ISO datetime
  items: ItemCarritoResponse[];
}
```

### OrdenResponse
```typescript
interface ItemOrdenResponse {
  id: number;
  varianteId: number;
  varianteColor: string;
  varianteTalla: string;
  productoNombre: string;
  cantidad: number;
  precioAlMomento: number;
}

interface OrdenResponse {
  id: number;
  usuarioId: number;
  carritoId: number | null;
  descuentoId: number | null;
  fechaCreacion: string; // ISO datetime
  montoFinal: number;
  estado: 'PENDIENTE' | 'CONFIRMADA' | 'ENTREGADA' | 'CANCELADA';
  items: ItemOrdenResponse[];
}
```

### ErrorResponse
```typescript
interface ErrorResponse {
  timestamp: string; // ISO datetime
  status: number;
  error: string;
  message: string;
}
```

---

## 5. ENUMERACIONES

```typescript
// Estado de usuario
type EstadoUsuario = 'ACTIVO' | 'INACTIVO';

// Rol de usuario
type RolUsuario = 'ADMIN' | 'CLIENTE';

// Estado de producto
type EstadoProducto = 'ACTIVO' | 'PAUSADO' | 'ELIMINADO';

// Estado de carrito
type EstadoCarrito = 'ACTIVO' | 'VACIO' | 'ABANDONADO' | 'CONVERTIDO';

// Estado de orden
type EstadoOrden = 'PENDIENTE' | 'CONFIRMADA' | 'ENTREGADA' | 'CANCELADA';

// Estado de descuento
type EstadoDescuento = 'ACTIVO' | 'EXPIRADO';

// Tipo de descuento
type TipoDescuento = 'PORCENTAJE' | 'FIJO';

// Estación del año
type Estacion = 'PRIMAVERA' | 'VERANO' | 'OTONO' | 'INVIERNO';
```

---

## 6. GUÍA DE INTEGRACIÓN REACT

### 6.1 Configuración del Cliente HTTP

```javascript
// src/services/api.js
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Interceptor: agrega token JWT automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor: maneja expiración de token
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 6.2 Variables de Entorno
```bash
# .env
REACT_APP_API_URL=http://localhost:8080/api
```

### 6.3 Servicio de Autenticación

```javascript
// src/services/authService.js
import api from './api';

export const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    const { token, username: user, rol } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ username: user, rol }));
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    const { token, username, rol } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ username, rol }));
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated: () => !!localStorage.getItem('token'),
  isAdmin: () => {
    const user = authService.getCurrentUser();
    return user?.rol === 'ADMIN';
  }
};
```

### 6.4 Servicios de Entidades

```javascript
// src/services/productoService.js
import api from './api';

export const productoService = {
  getAll: () => api.get('/productos'),
  getById: (id) => api.get(`/productos/${id}`),
  getByCategoria: (categoriaId) => api.get(`/productos/categoria/${categoriaId}`),
  getByMarca: (marcaId) => api.get(`/productos/marca/${marcaId}`),
  getByEstado: (estado) => api.get(`/productos/estado/${estado}`),
  isDisponible: (id) => api.get(`/productos/${id}/disponible`),
  create: (data) => api.post('/productos', data),
  update: (id, data) => api.put(`/productos/${id}`, data),
  delete: (id) => api.delete(`/productos/${id}`)
};
```

```javascript
// src/services/varianteService.js
import api from './api';

export const varianteService = {
  getAll: () => api.get('/variantes'),
  getById: (id) => api.get(`/variantes/${id}`),
  getPrecio: (id) => api.get(`/variantes/${id}/precio`),
  tieneStock: (id, cantidad) => api.get(`/variantes/${id}/stock/disponible`, { params: { cantidad } }),
  create: (data) => api.post('/variantes', data),
  update: (id, data) => api.put(`/variantes/${id}`, data),
  delete: (id) => api.delete(`/variantes/${id}`)
};
```

```javascript
// src/services/fotoService.js
import api from './api';

export const fotoService = {
  getAll: () => api.get('/fotos'),
  getById: (id) => api.get(`/fotos/${id}`),
  getByVariante: (varianteId) => api.get(`/fotos/variante/${varianteId}`),

  create: (varianteId, orden, archivo) => {
    const formData = new FormData();
    formData.append('varianteId', varianteId);
    formData.append('orden', orden);
    formData.append('archivo', archivo);
    return api.post('/fotos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  update: (id, varianteId, orden, archivo) => {
    const formData = new FormData();
    formData.append('varianteId', varianteId);
    formData.append('orden', orden);
    formData.append('archivo', archivo);
    return api.put(`/fotos/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  delete: (id) => api.delete(`/fotos/${id}`)
};
```

```javascript
// src/services/carritoService.js
import api from './api';

export const carritoService = {
  getAll: () => api.get('/carritos'),
  getById: (id) => api.get(`/carritos/${id}`),
  create: (usuarioId, descuentoId = null) =>
    api.post('/carritos', { usuarioId, descuentoId }),
  update: (id, data) => api.put(`/carritos/${id}`, data),
  delete: (id) => api.delete(`/carritos/${id}`),

  // Items
  agregarItem: (carritoId, idVariante, cantidad) =>
    api.post(`/carritos/${carritoId}/items`, { idVariante, cantidad }),
  eliminarItem: (carritoId, itemId) =>
    api.delete(`/carritos/${carritoId}/items/${itemId}`),
  actualizarItem: (carritoId, itemId, cantidad) =>
    api.put(`/carritos/${carritoId}/items/${itemId}`, null, { params: { cantidad } }),
  getItems: (carritoId) => api.get(`/carritos/${carritoId}/items`),

  // Operaciones
  getTotal: (carritoId) => api.get(`/carritos/${carritoId}/total`),
  vaciar: (carritoId) => api.post(`/carritos/${carritoId}/vaciar`),
  checkout: (carritoId) => api.post(`/carritos/${carritoId}/checkout`)
};
```

```javascript
// src/services/ordenService.js
import api from './api';

export const ordenService = {
  getAll: () => api.get('/ordenes'),
  getById: (id) => api.get(`/ordenes/${id}`),
  getByUsuario: (usuarioId) => api.get(`/ordenes/usuario/${usuarioId}`),
  getItems: (id) => api.get(`/ordenes/${id}/items`),
  getMontoFinal: (id) => api.get(`/ordenes/${id}/monto-final`),
  create: (data) => api.post('/ordenes', data),
  update: (id, data) => api.put(`/ordenes/${id}`, data),
  delete: (id) => api.delete(`/ordenes/${id}`),
  confirmar: (id) => api.post(`/ordenes/${id}/confirmar`),
  cancelar: (id) => api.post(`/ordenes/${id}/cancelar`)
};
```

```javascript
// src/services/descuentoService.js
import api from './api';

export const descuentoService = {
  getAll: () => api.get('/descuentos'),
  getActivos: () => api.get('/descuentos/activos'),
  getById: (id) => api.get(`/descuentos/${id}`),
  isVigente: (id) => api.get(`/descuentos/${id}/vigente`),
  calcular: (id, monto) => api.get(`/descuentos/${id}/calcular`, { params: { monto } }),
  create: (data) => api.post('/descuentos', data),
  update: (id, data) => api.put(`/descuentos/${id}`, data),
  delete: (id) => api.delete(`/descuentos/${id}`)
};
```

### 6.5 Custom Hooks

```javascript
// src/hooks/useProductos.js
import { useState, useEffect } from 'react';
import { productoService } from '../services/productoService';

export const useProductos = (filtro = null) => {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        setLoading(true);
        const response = filtro?.categoriaId
          ? await productoService.getByCategoria(filtro.categoriaId)
          : filtro?.marcaId
          ? await productoService.getByMarca(filtro.marcaId)
          : await productoService.getAll();
        setProductos(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Error al cargar productos');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [filtro?.categoriaId, filtro?.marcaId]);

  return { productos, loading, error };
};
```

```javascript
// src/hooks/useCarrito.js
import { useState, useEffect, useCallback } from 'react';
import { carritoService } from '../services/carritoService';
import { authService } from '../services/authService';

export const useCarrito = (carritoId) => {
  const [carrito, setCarrito] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchCarrito = useCallback(async () => {
    if (!carritoId) return;
    try {
      setLoading(true);
      const response = await carritoService.getById(carritoId);
      setCarrito(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cargar carrito');
    } finally {
      setLoading(false);
    }
  }, [carritoId]);

  useEffect(() => { fetchCarrito(); }, [fetchCarrito]);

  const agregarItem = async (varianteId, cantidad) => {
    try {
      await carritoService.agregarItem(carritoId, varianteId, cantidad);
      await fetchCarrito();
    } catch (err) {
      throw err;
    }
  };

  const eliminarItem = async (itemId) => {
    await carritoService.eliminarItem(carritoId, itemId);
    await fetchCarrito();
  };

  const checkout = async () => {
    const response = await carritoService.checkout(carritoId);
    await fetchCarrito();
    return response.data;
  };

  return { carrito, loading, error, agregarItem, eliminarItem, checkout, refetch: fetchCarrito };
};
```

### 6.6 Componente de Imagen desde Base64

```javascript
// src/components/ProductImage.jsx
const ProductImage = ({ foto, alt, className }) => {
  if (!foto) return <div className="no-image">Sin imagen</div>;

  return (
    <img
      src={`data:${foto.tipoContenido};base64,${foto.datos}`}
      alt={alt || foto.nombre}
      className={className}
    />
  );
};

export default ProductImage;
```

### 6.7 Contexto de Autenticación

```javascript
// src/context/AuthContext.jsx
import { createContext, useContext, useState } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(authService.getCurrentUser());

  const login = async (username, password) => {
    const data = await authService.login(username, password);
    setUser({ username: data.username, rol: data.rol });
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      isAuthenticated: !!user,
      isAdmin: user?.rol === 'ADMIN'
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### 6.8 Rutas Protegidas

```javascript
// src/components/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (requireAdmin && !isAdmin) return <Navigate to="/unauthorized" replace />;

  return children;
};
```

---

## 7. MANEJO DE ERRORES

### Tabla de Códigos HTTP
| Código | Significado | Causa Común | Acción en UI |
|--------|------------|-------------|--------------|
| 200 | OK | Éxito | Procesar respuesta |
| 201 | Created | Recurso creado | Redirigir o mostrar confirmación |
| 204 | No Content | Eliminado/Vacío | Actualizar lista local |
| 400 | Bad Request | Validación fallida / regla de negocio | Mostrar mensaje específico del backend |
| 401 | Unauthorized | Token inválido/expirado | Redirigir a /login |
| 403 | Forbidden | Sin permisos o cuenta inactiva | Mostrar "Acceso denegado" |
| 404 | Not Found | Recurso no existe | Mostrar 404 o volver a lista |
| 405 | Method Not Allowed | Método HTTP incorrecto | Error de desarrollo |
| 409 | Conflict | Dato duplicado (username, nombre) | Mostrar "Ya existe" |
| 413 | Payload Too Large | Archivo > 10MB | Mostrar límite de tamaño |
| 415 | Unsupported Media Type | Olvidaste multipart/form-data | Error de implementación |
| 500 | Server Error | Error interno | Mostrar error genérico |

### Helper de Manejo de Errores
```javascript
// src/utils/errorHandler.js
export const getErrorMessage = (error) => {
  if (error.response) {
    const { status, data } = error.response;
    switch (status) {
      case 400: return data.message || 'Datos inválidos';
      case 401: return 'Sesión expirada. Inicia sesión nuevamente';
      case 403: return data.message || 'No tienes permisos para esta acción';
      case 404: return data.message || 'Recurso no encontrado';
      case 409: return data.message || 'Ya existe un registro con esos datos';
      case 413: return 'El archivo es demasiado grande (máximo 10MB)';
      case 415: return 'Formato de archivo no soportado';
      case 500: return 'Error del servidor. Intenta nuevamente más tarde';
      default: return data.message || 'Error desconocido';
    }
  }
  if (error.request) return 'Sin respuesta del servidor';
  return error.message || 'Error de conexión';
};
```

---

## 8. FLUJOS DE NEGOCIO CLAVE

### Flujo de Compra Completo
```
1. Usuario registrado/logueado (token JWT)
2. GET /api/productos → mostrar catálogo
3. GET /api/variantes/{id} → ver variante específica
4. POST /api/carritos → crear carrito (si no existe)
5. POST /api/carritos/{id}/items → agregar ítems
6. GET /api/carritos/{id}/total → ver total con descuento
7. POST /api/carritos/{id}/checkout → COMPRAR
   └── Crea Orden (PENDIENTE), descuenta stock, limpia carrito
8. POST /api/ordenes/{id}/confirmar → confirmar orden
9. POST /api/ordenes/{id}/cancelar → cancelar (si necesario, restaura stock)
```

### Estados de Carrito
```
VACIO → (agregar ítems) → ACTIVO
ACTIVO → (checkout) → CONVERTIDO
ACTIVO → (sin actividad 7 días) → ABANDONADO [automático]
ACTIVO → (vaciar) → VACIO
```

### Estados de Orden
```
PENDIENTE → (confirmar) → CONFIRMADA
CONFIRMADA → (entregar) → ENTREGADA [solo admin/backend]
PENDIENTE/CONFIRMADA → (cancelar) → CANCELADA [restaura stock]
```

### Reglas de Negocio Importantes
1. **Un usuario solo puede tener UN carrito ACTIVO o VACIO** a la vez
2. **El stock se descuenta en el checkout**, no al agregar al carrito
3. **Los precios se capturan como snapshot** (precioUnitario en ítem carrito, precioAlMomento en ítem orden)
4. **Cancelar una orden restaura el stock** (si no está ENTREGADA)
5. **Las fotos se almacenan como binario** y se devuelven en Base64
6. **Los descuentos expiran automáticamente** a diario (job)
7. **Los carritos inactivos 7 días** se marcan ABANDONADO (job semanal)

---

## 9. TROUBLESHOOTING

### Error: 401 en toda petición
- Verificar que el token está en localStorage con key `token`
- Verificar que el interceptor agrega el header `Authorization: Bearer <token>`
- Token puede haber expirado (dura 24 horas)

### Error: 403 en endpoints de carrito/orden
- El usuario está intentando acceder a un recurso que no le pertenece
- El usuario tiene rol CLIENTE e intenta hacer operaciones de ADMIN

### Error: 415 al subir fotos
- Asegurarse de usar `Content-Type: multipart/form-data` (axios lo hace automático con FormData)
- No sobrescribir el header Content-Type manualmente en el request de foto

### Error: 400 "usuario ya tiene un carrito activo"
- Buscar primero si el usuario ya tiene carrito: `GET /api/carritos`
- Filtrar por `estado === 'ACTIVO' || estado === 'VACIO'`
- Si existe, usar ese en lugar de crear uno nuevo

### Error: 400 al agregar ítem al carrito
- Verificar que el carrito no está en estado `CONVERTIDO` ni `ABANDONADO`
- Verificar que el producto de la variante tiene `estado === 'ACTIVO'`

### Las imágenes no se muestran
- Verificar que se usa: `data:${foto.tipoContenido};base64,${foto.datos}`
- El campo `datos` ya viene en Base64 desde la API (no re-encodear)

### CORS errors
- El backend permite todos los orígenes (`*`)
- Si hay error CORS, verificar que el backend está corriendo en puerto 8080
- No enviar credenciales con `*` en allowed origins (revisar config CORS si cambia)

---

## 10. PREGUNTAS FRECUENTES

**¿Cómo sé el usuarioId del usuario logueado?**
> El token JWT contiene el `username`. Con ese username podés hacer `GET /api/usuarios` (admin) o usar el username para filtrar. En el registro/login, la respuesta incluye `username` y `rol`, pero no el `id`. Para obtener el `id` debería existir un endpoint `GET /api/usuarios/me` o usar el username como identificador en carritos.

> **Nota importante**: Al crear un carrito, el `usuarioId` debe ser del usuario autenticado. El backend valida la propiedad.

**¿Cómo mostrar todas las fotos de un producto?**
> 1. `GET /api/variantes` o `GET /api/variantes/{id}` para obtener variantes del producto
> 2. `GET /api/fotos/variante/{varianteId}` para fotos de cada variante
> 3. Ordenar por el campo `orden` (Integer)

**¿Cómo aplicar un descuento al carrito?**
> 1. `GET /api/descuentos/activos` para listar disponibles
> 2. `PUT /api/carritos/{id}` con `{ usuarioId, descuentoId }` para aplicar
> 3. `GET /api/carritos/{id}/total` para ver el total con descuento aplicado

**¿Cómo sé si una variante tiene stock suficiente?**
> `GET /api/variantes/{id}/stock/disponible?cantidad=5` → responde `true` o `false`

**¿El checkout valida stock automáticamente?**
> No: el stock no se valida al agregar al carrito. **Sí** se descuenta en el checkout. Si no hay stock suficiente al hacer checkout, la operación fallará en el servicio (BusinessRuleException).

**¿Puedo usar el Swagger para probar?**
> Sí: `http://localhost:8080/swagger-ui.html`
> 1. Hacer login con `POST /api/auth/login`
> 2. Copiar el token
> 3. Hacer click en "Authorize" (ícono de candado)
> 4. Escribir `Bearer <token>` y confirmar
> 5. Todos los endpoints protegidos funcionarán

---

## APÉNDICE: PROMPT OPTIMIZADO PARA FRONTEND

El siguiente prompt está listo para copiar y pegar cuando trabajen con Claude Code en el proyecto frontend:

---

```
# CONTEXTO BACKEND - TREKKING ECOMMERCE

Estoy implementando el frontend React para un ecommerce de trekking.
El backend es Spring Boot (Java 17) con autenticación JWT.

## BASE URL
http://localhost:8080/api

## AUTENTICACIÓN
- JWT Bearer Token (24 horas de duración)
- Header: Authorization: Bearer <token>
- Login: POST /api/auth/login → { username, password } → { token, username, rol }
- Roles: ADMIN, CLIENTE

## ENDPOINTS PRINCIPALES

### Auth (públicos)
- POST /api/auth/login   → { username, password }
- POST /api/auth/register → { username, email, password(min8+mayus+digit), nombre, apellido }

### Catálogo (públicos)
- GET /api/categorias
- GET /api/marcas
- GET /api/productos
- GET /api/productos/categoria/{id}
- GET /api/productos/marca/{id}
- GET /api/productos/{id}/disponible → boolean
- GET /api/variantes
- GET /api/variantes/{id}
- GET /api/variantes/{id}/stock/disponible?cantidad=N → boolean
- GET /api/fotos/variante/{varianteId}  ← fotos en Base64

### Carrito (autenticado)
- POST /api/carritos → { usuarioId, descuentoId? }
- GET  /api/carritos
- POST /api/carritos/{id}/items → { idVariante, cantidad }
- DELETE /api/carritos/{id}/items/{itemId}
- PUT  /api/carritos/{id}/items/{itemId}?cantidad=N
- GET  /api/carritos/{id}/total → BigDecimal
- POST /api/carritos/{id}/checkout → OrdenResponse (DESCUENTA STOCK)
- POST /api/carritos/{id}/vaciar

### Órdenes (autenticado)
- GET  /api/ordenes
- GET  /api/ordenes/{id}
- GET  /api/ordenes/usuario/{userId}
- POST /api/ordenes/{id}/confirmar
- POST /api/ordenes/{id}/cancelar (RESTAURA STOCK)
- DELETE /api/ordenes/{id}

### Descuentos (autenticado para /activos, admin para resto)
- GET /api/descuentos/activos
- GET /api/descuentos/{id}/calcular?monto=N → BigDecimal

## REGLAS DE NEGOCIO CLAVE
1. Usuario solo puede tener UN carrito ACTIVO o VACIO
2. Stock se descuenta en checkout, no al agregar al carrito
3. Cancelar orden restaura stock
4. Fotos vienen en Base64: <img src={`data:${foto.tipoContenido};base64,${foto.datos}`} />
5. Precios capturados como snapshot al momento de agregar/comprar

## RESPUESTA DE ERROR
{ timestamp, status, error, message }

Basándote en este contexto, [DESCRIBE TU TAREA AQUÍ]
```

---

*Documento generado automáticamente mediante análisis exhaustivo del proyecto backend.*
*Fecha: 2026-06-14*
