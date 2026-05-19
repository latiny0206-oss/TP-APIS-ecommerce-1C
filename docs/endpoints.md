# Tabla de Endpoints — Trekking Ecommerce API

> **Total de endpoints:** 69  
> **Paquete base:** `com.trekking.ecommerce.controller`  
> **Seguridad:** cruzada entre `SecurityBeansConfig.java` (FilterChain) y anotaciones `@PreAuthorize` por método/clase.

---

### AuthController

Base path: `/api/auth`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| POST | `/api/auth/login` | `LoginRequest` (body) | `AuthResponse` | Público | Autentica credenciales y devuelve JWT |
| POST | `/api/auth/register` | `RegisterRequest` (body) | `AuthResponse` | Público | Registra usuario con rol CLIENTE y devuelve JWT |

---

### ProductoController

Base path: `/api/productos`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/productos` | — | `List<ProductoResponse>` | Público | Lista todos los productos |
| GET | `/api/productos/{id}` | `{id}` (path) | `ProductoResponse` | Público | Obtiene un producto por ID |
| GET | `/api/productos/categoria/{categoriaId}` | `{categoriaId}` (path) | `List<ProductoResponse>` | Público | Lista productos por categoría |
| GET | `/api/productos/marca/{marcaId}` | `{marcaId}` (path) | `List<ProductoResponse>` | Público | Lista productos por marca |
| GET | `/api/productos/estado/{estado}` | `{estado}` (path) `EstadoProducto` | `List<ProductoResponse>` | Público | Lista productos por estado (ACTIVO, PAUSADO, ELIMINADO) |
| GET | `/api/productos/{id}/disponible` | `{id}` (path) | `Boolean` | Público | Verifica si un producto tiene al menos una variante con stock |
| POST | `/api/productos` | `ProductoRequest` (body) | `ProductoResponse` | ROLE_ADMIN | Crea un producto nuevo |
| PUT | `/api/productos/{id}` | `{id}` (path) + `ProductoRequest` (body) | `ProductoResponse` | ROLE_ADMIN | Actualiza un producto existente |
| DELETE | `/api/productos/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina un producto |

---

### CarritoController

Base path: `/api/carritos`  
Extiende `AuthenticatedController` — todos los endpoints validan propiedad del recurso en runtime (`validarPropietario()`). ADMIN puede acceder a cualquier carrito; CLIENTE solo al propio.

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/carritos` | — | `List<CarritoResponse>` | Autenticado | Lista carritos (ADMIN: todos; CLIENTE: solo los propios) |
| GET | `/api/carritos/{id}` | `{id}` (path) | `CarritoResponse` | Autenticado | Obtiene un carrito por ID (validación de propietario) |
| POST | `/api/carritos` | `CarritoRequest` (body) | `CarritoResponse` | Autenticado | Crea un carrito nuevo para el usuario autenticado |
| PUT | `/api/carritos/{id}` | `{id}` (path) + `CarritoRequest` (body) | `CarritoResponse` | Autenticado | Actualiza datos de un carrito (validación de propietario) |
| DELETE | `/api/carritos/{id}` | `{id}` (path) | — | Autenticado | Elimina un carrito (validación de propietario) |
| GET | `/api/carritos/{id}/items` | `{id}` (path) | `List<ItemCarritoResponse>` | Autenticado | Lista los ítems de un carrito (validación de propietario) |
| GET | `/api/carritos/{id}/total` | `{id}` (path) | `BigDecimal` | Autenticado | Calcula el monto total del carrito (validación de propietario) |
| POST | `/api/carritos/{id}/items` | `{id}` (path) + `ItemCarritoRequest` (body) | `ItemCarritoResponse` | Autenticado | Agrega un ítem al carrito (validación de propietario) |
| PUT | `/api/carritos/{id}/items/{idItem}` | `{id}`, `{idItem}` (path) + `cantidad` (param) | `ItemCarritoResponse` | Autenticado | Actualiza la cantidad de un ítem (validación de propietario) |
| DELETE | `/api/carritos/{id}/items/{idItem}` | `{id}`, `{idItem}` (path) | — | Autenticado | Elimina un ítem del carrito (validación de propietario) |
| POST | `/api/carritos/{id}/vaciar` | `{id}` (path) | — | Autenticado | Vacía todos los ítems del carrito (validación de propietario) |
| POST | `/api/carritos/{id}/checkout` | `{id}` (path) | `OrdenResponse` | Autenticado | Realiza el checkout: convierte el carrito en una orden (validación de propietario) |

---

### UsuarioController

Base path: `/api/usuarios`  
`@PreAuthorize("hasRole('ADMIN')")` aplicado a nivel de clase — todos los métodos requieren ROLE_ADMIN.

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/usuarios` | — | `List<UsuarioResponse>` | ROLE_ADMIN | Lista todos los usuarios registrados |
| GET | `/api/usuarios/{id}` | `{id}` (path) | `UsuarioResponse` | ROLE_ADMIN | Obtiene un usuario por ID |
| POST | `/api/usuarios` | `UsuarioRequest` (body) | `UsuarioResponse` | ROLE_ADMIN | Crea un usuario con cualquier rol y estado |
| PUT | `/api/usuarios/{id}` | `{id}` (path) + `UsuarioRequest` (body) | `UsuarioResponse` | ROLE_ADMIN | Actualiza datos de un usuario |
| DELETE | `/api/usuarios/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina un usuario |

---

### CategoriaController

Base path: `/api/categorias`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/categorias` | — | `List<CategoriaResponse>` | Público | Lista todas las categorías |
| GET | `/api/categorias/{id}` | `{id}` (path) | `CategoriaResponse` | Público | Obtiene una categoría por ID |
| POST | `/api/categorias` | `CategoriaRequest` (body) | `CategoriaResponse` | ROLE_ADMIN | Crea una categoría nueva |
| PUT | `/api/categorias/{id}` | `{id}` (path) + `CategoriaRequest` (body) | `CategoriaResponse` | ROLE_ADMIN | Actualiza una categoría |
| DELETE | `/api/categorias/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina una categoría |

---

### MarcaController

Base path: `/api/marcas`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/marcas` | — | `List<MarcaResponse>` | Público | Lista todas las marcas |
| GET | `/api/marcas/{id}` | `{id}` (path) | `MarcaResponse` | Público | Obtiene una marca por ID |
| POST | `/api/marcas` | `MarcaRequest` (body) | `MarcaResponse` | ROLE_ADMIN | Crea una marca nueva |
| PUT | `/api/marcas/{id}` | `{id}` (path) + `MarcaRequest` (body) | `MarcaResponse` | ROLE_ADMIN | Actualiza una marca |
| DELETE | `/api/marcas/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina una marca |

---

### VarianteProductoController

Base path: `/api/variantes`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/variantes` | — | `List<VarianteProductoResponse>` | Público | Lista todas las variantes de producto |
| GET | `/api/variantes/{id}` | `{id}` (path) | `VarianteProductoResponse` | Público | Obtiene una variante por ID |
| GET | `/api/variantes/{id}/precio` | `{id}` (path) | `BigDecimal` | Público | Devuelve el precio de una variante |
| GET | `/api/variantes/{id}/stock/disponible` | `{id}` (path) + `cantidad` (param) | `Boolean` | Público | Verifica si hay suficiente stock para la cantidad pedida |
| POST | `/api/variantes` | `VarianteProductoRequest` (body) | `VarianteProductoResponse` | ROLE_ADMIN | Crea una variante nueva (color, talla, material, stock, precio, estación) |
| PUT | `/api/variantes/{id}` | `{id}` (path) + `VarianteProductoRequest` (body) | `VarianteProductoResponse` | ROLE_ADMIN | Actualiza una variante existente |
| DELETE | `/api/variantes/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina una variante |

---

### DescuentoController

Base path: `/api/descuentos`

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/descuentos` | — | `List<DescuentoResponse>` | ROLE_ADMIN | Lista todos los descuentos (activos e inactivos) |
| GET | `/api/descuentos/activos` | — | `List<DescuentoResponse>` | Autenticado | Lista descuentos con estado ACTIVO y fecha vigente |
| GET | `/api/descuentos/{id}` | `{id}` (path) | `DescuentoResponse` | ROLE_ADMIN | Obtiene un descuento por ID |
| GET | `/api/descuentos/{id}/vigente` | `{id}` (path) | `Boolean` | ROLE_ADMIN | Verifica si un descuento está vigente por fecha |
| GET | `/api/descuentos/{id}/calcular` | `{id}` (path) + `monto` (param `BigDecimal`) | `BigDecimal` | ROLE_ADMIN | Calcula el monto resultante al aplicar el descuento |
| POST | `/api/descuentos` | `DescuentoRequest` (body) | `DescuentoResponse` | ROLE_ADMIN | Crea un descuento nuevo |
| PUT | `/api/descuentos/{id}` | `{id}` (path) + `DescuentoRequest` (body) | `DescuentoResponse` | ROLE_ADMIN | Actualiza un descuento |
| DELETE | `/api/descuentos/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina un descuento |

---

### OrdenController

Base path: `/api/ordenes`  
Extiende `AuthenticatedController`. ADMIN puede ver y gestionar todas las órdenes; CLIENTE solo las propias (validación via `validarPropietario()`).

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/ordenes` | — | `List<OrdenResponse>` | Autenticado | Lista órdenes (ADMIN: todas; CLIENTE: solo las propias) |
| GET | `/api/ordenes/{id}` | `{id}` (path) | `OrdenResponse` | Autenticado | Obtiene una orden por ID (validación de propietario) |
| GET | `/api/ordenes/{id}/items` | `{id}` (path) | `List<ItemOrdenResponse>` | Autenticado | Lista los ítems de una orden (validación de propietario) |
| GET | `/api/ordenes/{id}/monto-final` | `{id}` (path) | `BigDecimal` | Autenticado | Devuelve el monto final de una orden (validación de propietario) |
| GET | `/api/ordenes/usuario/{idUsuario}` | `{idUsuario}` (path) | `List<OrdenResponse>` | Autenticado | Historial de órdenes de un usuario (validación de propietario) |
| POST | `/api/ordenes` | `OrdenRequest` (body) | `OrdenResponse` | ⚠️ ROLE_ADMIN | Crea una orden directamente (solo ADMIN) |
| PUT | `/api/ordenes/{id}` | `{id}` (path) + `OrdenRequest` (body) | `OrdenResponse` | ⚠️ ROLE_ADMIN | Actualiza una orden (solo ADMIN) |
| DELETE | `/api/ordenes/{id}` | `{id}` (path) | — | Autenticado | Elimina una orden (validación de propietario; ADMIN puede eliminar cualquiera) |
| POST | `/api/ordenes/{id}/confirmar` | `{id}` (path) | `OrdenResponse` | Autenticado | Confirma una orden (validación de propietario) |
| POST | `/api/ordenes/{id}/cancelar` | `{id}` (path) | `OrdenResponse` | Autenticado | Cancela una orden (validación de propietario) |

---

### FotoController

Base path: `/api/fotos`  
El `POST` y `PUT` consumen `multipart/form-data` (no JSON).

| Método | URL | Request Body / Params | Response | Auth | Descripción |
|--------|-----|-----------------------|----------|------|-------------|
| GET | `/api/fotos` | — | `List<FotoResponse>` | Público | Lista todas las fotos con datos en Base64 |
| GET | `/api/fotos/{id}` | `{id}` (path) | `FotoResponse` | Público | Obtiene una foto por ID (datos en Base64) |
| GET | `/api/fotos/variante/{varianteId}` | `{varianteId}` (path) | `List<FotoResponse>` | Público | Lista las fotos de una variante de producto |
| POST | `/api/fotos` | `varianteId`, `orden` (params) + `archivo` (multipart) | `FotoResponse` | ROLE_ADMIN | Sube una foto y la asocia a una variante |
| PUT | `/api/fotos/{id}` | `{id}` (path) + `varianteId`, `orden` (params) + `archivo` (multipart) | `FotoResponse` | ROLE_ADMIN | Reemplaza la imagen de una foto existente |
| DELETE | `/api/fotos/{id}` | `{id}` (path) | — | ROLE_ADMIN | Elimina una foto |

---

## Resumen de accesos

### Endpoints públicos (`permitAll`)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/productos`
- `GET /api/productos/{id}`
- `GET /api/productos/categoria/{categoriaId}`
- `GET /api/productos/marca/{marcaId}`
- `GET /api/productos/estado/{estado}`
- `GET /api/productos/{id}/disponible`
- `GET /api/categorias`
- `GET /api/categorias/{id}`
- `GET /api/marcas`
- `GET /api/marcas/{id}`
- `GET /api/variantes`
- `GET /api/variantes/{id}`
- `GET /api/variantes/{id}/precio`
- `GET /api/variantes/{id}/stock/disponible`
- `GET /api/fotos`
- `GET /api/fotos/{id}`
- `GET /api/fotos/variante/{varianteId}`

### Requieren autenticación (ROLE_ADMIN o ROLE_CLIENTE)

- `GET /api/carritos`
- `GET /api/carritos/{id}`
- `POST /api/carritos`
- `PUT /api/carritos/{id}`
- `DELETE /api/carritos/{id}`
- `GET /api/carritos/{id}/items`
- `GET /api/carritos/{id}/total`
- `POST /api/carritos/{id}/items`
- `PUT /api/carritos/{id}/items/{idItem}`
- `DELETE /api/carritos/{id}/items/{idItem}`
- `POST /api/carritos/{id}/vaciar`
- `POST /api/carritos/{id}/checkout`
- `GET /api/descuentos/activos`
- `GET /api/ordenes`
- `GET /api/ordenes/{id}`
- `GET /api/ordenes/{id}/items`
- `GET /api/ordenes/{id}/monto-final`
- `GET /api/ordenes/usuario/{idUsuario}`
- `DELETE /api/ordenes/{id}`
- `POST /api/ordenes/{id}/confirmar`
- `POST /api/ordenes/{id}/cancelar`

### Solo ROLE_ADMIN

- `POST /api/productos`
- `PUT /api/productos/{id}`
- `DELETE /api/productos/{id}`
- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`
- `POST /api/categorias`
- `PUT /api/categorias/{id}`
- `DELETE /api/categorias/{id}`
- `POST /api/marcas`
- `PUT /api/marcas/{id}`
- `DELETE /api/marcas/{id}`
- `POST /api/variantes`
- `PUT /api/variantes/{id}`
- `DELETE /api/variantes/{id}`
- `GET /api/descuentos`
- `GET /api/descuentos/{id}`
- `GET /api/descuentos/{id}/vigente`
- `GET /api/descuentos/{id}/calcular`
- `POST /api/descuentos`
- `PUT /api/descuentos/{id}`
- `DELETE /api/descuentos/{id}`
- `POST /api/ordenes` ⚠️
- `PUT /api/ordenes/{id}` ⚠️
- `POST /api/fotos`
- `PUT /api/fotos/{id}`
- `DELETE /api/fotos/{id}`

---

## Notas al pie

### ⚠️ Inconsistencias detectadas entre FilterChain y `@PreAuthorize`

| Endpoint | FilterChain (`SecurityBeansConfig`) | `@PreAuthorize` en método | Comportamiento efectivo |
|----------|-------------------------------------|--------------------------|------------------------|
| `POST /api/ordenes` | `anyRequest().authenticated()` — permite cualquier usuario autenticado | `@PreAuthorize("hasRole('ADMIN')")` — restringe a ADMIN | **ROLE_ADMIN** (el `@PreAuthorize` prevalece al ser más restrictivo, bloqueando a clientes autenticados) |
| `PUT /api/ordenes/{id}` | `anyRequest().authenticated()` — permite cualquier usuario autenticado | `@PreAuthorize("hasRole('ADMIN')")` — restringe a ADMIN | **ROLE_ADMIN** (ídem anterior) |

**Explicación:** La creación y modificación directa de órdenes está reservada exclusivamente al ADMIN. Los clientes crean órdenes únicamente a través del flujo de checkout (`POST /api/carritos/{id}/checkout`). La inconsistencia no genera un bug de seguridad — Spring Security evalúa ambas capas y aplica la más restrictiva — pero refleja que el FilterChain no fue actualizado para alinear los permisos de `POST` y `PUT` de `/api/ordenes/**` con el nivel `hasRole('ADMIN')`, delegando toda la restricción al `@PreAuthorize`.
