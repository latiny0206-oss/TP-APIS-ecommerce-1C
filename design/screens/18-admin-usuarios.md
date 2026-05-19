---
screen: Admin — Gestión de Usuarios
order: 18
device: responsive
status: pending
---

## Descripción
Panel de administración para ver y gestionar todos los usuarios registrados. El admin puede crear usuarios (incluyendo otros admins), editar datos, cambiar estado y rol, y eliminar cuentas.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/usuarios` | ADMIN | Lista de todos los usuarios |
| `GET /api/usuarios/{id}` | ADMIN | Datos de un usuario específico |
| `POST /api/usuarios` | ADMIN | Crear usuario (puede asignar cualquier rol) |
| `PUT /api/usuarios/{id}` | ADMIN | Editar usuario |
| `DELETE /api/usuarios/{id}` | ADMIN | Eliminar usuario |

**Datos de UsuarioRequest (para crear/editar):**
- `username` — único
- `email` — único, formato válido
- `password` — min 8 chars, uppercase, digit
- `nombre`, `apellido`
- `rol` — ADMIN o CLIENTE (a diferencia del registro público, el admin puede asignar ADMIN)
- `estado` — ACTIVO o INACTIVO

**Datos de UsuarioResponse (por fila):**
- `id`, `username`, `email`, `nombre`, `apellido`, `rol`, `estado`

**Notas de validación:**
- Username único: 409 si duplicado
- Email único: 409 si duplicado
- Password con regex (uppercase + digit)
- Estado INACTIVO → el usuario no puede iniciar sesión (DisabledException)

## Flujo de navegación
- **Viene desde:** AdminSidebar ("Usuarios"), Admin Dashboard (contador de clientes)
- **Puede ir a:** Admin Dashboard
- **Trigger:** Navegación desde sidebar

## Componentes UI
- AdminSidebar
- NavBar admin
- Barra de búsqueda + filtro por rol + botón "Nuevo usuario"
- DataTable: ID, Username, Nombre completo, Email, Rol (badge), Estado (badge), Acciones
- Modal crear/editar usuario con todos los campos
- ConfirmDialog para eliminar
- Toggle de estado (ACTIVO/INACTIVO) directo en la fila

## Estados
- [ ] Default — tabla de usuarios cargada
- [ ] Filtrado — por rol (ADMIN/CLIENTE) o por estado
- [ ] Modal crear — formulario vacío
- [ ] Modal editar — formulario pre-rellenado (sin mostrar password actual)
- [ ] Guardando — spinner
- [ ] Error duplicado — 409 con mensaje específico
- [ ] Confirm eliminar

## Notas de UX
- Al editar un usuario existente, el campo password debe ser opcional (si se deja vacío, no se cambia la contraseña). El backend actual siempre requiere password en el UsuarioRequest; puede necesitar lógica adicional.
- El estado INACTIVO impide el login del usuario. Mostrar esto claramente en la fila.
- No permitir que el admin se elimine a sí mismo o que desactive su propia cuenta.
- El rol ADMIN en la badge puede ser naranja (#E76F51) para diferenciarlo del rol CLIENTE verde (#40916C).

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — User Management** for TrailForge back-office. Desktop layout with AdminSidebar.
>
> **Layout:**
> 1. AdminSidebar, "Usuarios" active.
> 2. Page header: "Gestión de Usuarios" Sora H1 + "124 usuarios registrados" + "Nuevo usuario" PrimaryButton right.
> 3. Filter row: search input + Role filter select ("Todos los roles" / "ADMIN" / "CLIENTE") + Status filter ("Todos" / "Activo" / "Inactivo").
> 4. **DataTable**:
>    Columns: "ID" | "Usuario" | "Nombre" | "Email" | "Rol" | "Estado" | "Acciones".
>    Row examples:
>    - "1" | "admin.main" | "Admin Principal" | "admin@trailforge.com" | RolBadge "ADMIN" #E76F51 bg white text | EstadoBadge "ACTIVO" #40916C | ✎ 🗑 (no delete on own account)
>    - "2" | "juan.garcia" | "Juan García" | "juan@gmail.com" | RolBadge "CLIENTE" #2D6A4F bg white text | Toggle switch "ACTIVO" (green) | ✎ 🗑
>    - "3" | "maria.lopez" | "María López" | "maria@gmail.com" | RolBadge "CLIENTE" | Toggle switch "INACTIVO" (gray #8C8880) | ✎ 🗑
>    Toggle switch: when clicked calls PUT /api/usuarios/{id} with estado toggled.
> 5. **Create/Edit user modal** (520px):
>    "Nuevo Usuario" / "Editar Usuario" Sora H2. Form: 2-col row (Nombre + Apellido), Username input, Email input, Password input (with note "Dejar vacío para no cambiar" when editing), 2-col row (Rol select ADMIN/CLIENTE + Estado select ACTIVO/INACTIVO). "Guardar" + "Cancelar". Validation error: "Este email ya está registrado." #C1121F.
> 6. **Delete ConfirmDialog**: "¿Eliminar al usuario juan.garcia? Esta acción no se puede deshacer." DangerButton + SecondaryButton.
