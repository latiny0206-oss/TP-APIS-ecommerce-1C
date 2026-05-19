---
screen: Perfil de Usuario
order: 10
device: responsive
status: pending
---

## Descripción
Pantalla donde el usuario autenticado ve y puede editar sus datos personales. También muestra accesos directos a sus órdenes y carrito activo.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/usuarios/{id}` | ADMIN | Datos del usuario (nombre, apellido, username, email, rol, estado) |
| `PUT /api/usuarios/{id}` | ADMIN | Actualizar datos del usuario |

> **Nota:** El backend no expone un endpoint `GET /api/auth/me` o similar para que el cliente obtenga su propio perfil. Los datos del usuario autenticado (`username`, `rol`) vienen del JWT (`AuthResponse`). Para ver o editar el perfil completo (nombre, apellido, email), el endpoint `GET /api/usuarios/{id}` requiere rol ADMIN. Esta es una limitación del backend actual; la pantalla asume que el frontend almacena los datos de registro en el estado local o que esta pantalla se implementa cuando se añada el endpoint de perfil propio.

**Datos del usuario (desde JWT + estado local o futuro endpoint):**
- `username`
- `email`
- `nombre`
- `apellido`
- `rol` (CLIENTE / ADMIN)
- `estado` (ACTIVO / INACTIVO)

## Flujo de navegación
- **Viene desde:** NavBar (ícono de usuario/avatar), Mis Órdenes (breadcrumb)
- **Puede ir a:** Mis Órdenes, Carrito, Login (botón "Cerrar sesión")
- **Trigger:** Click en avatar/nombre de usuario en NavBar

## Componentes UI
- NavBar
- Avatar circular (iniciales del usuario o imagen placeholder)
- Chip de rol ("Cliente" en verde, "Admin" en naranja)
- Campos de datos en modo lectura (nombre, apellido, username, email)
- Botón "Editar perfil" (abre formulario inline o modal)
- Formulario de edición (nombre, apellido, email, password)
- Sección de accesos rápidos: "Mis órdenes" / "Mi carrito"
- DangerButton "Cerrar sesión" (limpia el JWT del storage)

## Estados
- [ ] Default — datos del usuario en modo lectura
- [ ] Edición — campos habilitados como inputs, botones "Guardar" y "Cancelar"
- [ ] Guardando — spinner en botón
- [ ] Error al guardar — toast de error
- [ ] Guardado exitoso — toast verde "Perfil actualizado"

## Notas de UX
- "Cerrar sesión" debe limpiar el JWT del localStorage/sessionStorage y redirigir a Home (sin autenticar).
- El campo `username` no debería ser editable (es el identificador de login).
- El rol y estado del usuario son read-only; no pueden cambiarse desde esta pantalla.
- Esta pantalla debería expandirse cuando el backend implemente un endpoint `GET /api/perfil` propio del usuario autenticado.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **User Profile** page for TrailForge. Device: mobile-first, responsive.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. **Profile header card** (white surface, light shadow): centered avatar circle 80×80px with user initials "JG" on #2D6A4F background, white Sora H2. Below: "Juan García" Sora H2 #1A1A18. Below: "@juan.garcia" Inter body #5C5C56. Chip badge: "Cliente" pill #40916C light background #40916C text.
> 3. **Personal data card** (white surface, 8px radius):
>    - "Mis datos" Sora H2 + "Editar" link-button right-aligned in #2D6A4F
>    - Read-only rows (label + value): "Nombre" / "Juan", "Apellido" / "García", "Email" / "juan@gmail.com", "Usuario" / "juan.garcia"
>    - All rows separated by #D4CFC8 hairline dividers
>    - Edit mode (shown when "Editar" clicked): FormInputs replace text, "Guardar cambios" PrimaryButton + "Cancelar" SecondaryButton at bottom of card
> 4. **Quick links card**:
>    - "Mis órdenes" row with order icon + chevron right → links to Mis Órdenes
>    - "Mi carrito" row with cart icon + chevron right → links to Carrito
>    - Each row: 48px height, #D4CFC8 divider between rows
> 5. **Danger zone card**:
>    - DangerButton "Cerrar sesión" outline style, #C1121F border and text, full-width. Logout icon left.
>
> **Desktop layout:** centered single-column layout max-width 600px, same structure.
> Emotional tone: organized, personal, like your basecamp — everything in its place.
