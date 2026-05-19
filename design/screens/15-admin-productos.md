---
screen: Admin — Gestión de Productos
order: 15
device: responsive
status: pending
---

## Descripción
Panel de administración para gestionar el catálogo de productos. El admin puede ver, crear, editar y eliminar productos. También accede a las variantes de cada producto.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/productos` | Público | Lista de todos los productos (todos los estados) |
| `POST /api/productos` | ADMIN | Crear nuevo producto |
| `PUT /api/productos/{id}` | ADMIN | Editar producto |
| `DELETE /api/productos/{id}` | ADMIN | Eliminar producto |
| `GET /api/categorias` | Público | Para select de categoría en formulario |
| `GET /api/marcas` | Público | Para select de marca en formulario |

**Datos de ProductoRequest:**
- `marcaId` (Long) — select de marca
- `categoriaId` (Long) — select de categoría
- `nombre` (String) — texto
- `descripcion` (String) — textarea
- `estado` (EstadoProducto) — ACTIVO / PAUSADO / ELIMINADO
- `precioBase` (BigDecimal) — número

**Datos de ProductoResponse (por fila de tabla):**
- `id`, `nombre`, `marcaNombre`, `categoriaNombre`, `estado`, `precioBase`

## Flujo de navegación
- **Viene desde:** AdminSidebar ("Productos"), Admin Dashboard (link "Ver todos")
- **Puede ir a:** Admin Variantes (click en "Ver variantes" de un producto), Admin Dashboard
- **Trigger:** Navegación desde sidebar o dashboard

## Componentes UI
- AdminSidebar
- NavBar admin
- Barra de acciones (buscador + botón "Nuevo producto")
- DataTable con columnas: ID, Nombre, Marca, Categoría, Estado (StatusBadge), Precio base, Acciones (editar/variantes/eliminar)
- Modal/drawer "Nuevo Producto" / "Editar Producto":
  - FormInput: Nombre
  - Select: Marca (opciones de GET /api/marcas)
  - Select: Categoría (opciones de GET /api/categorias)
  - Textarea: Descripción
  - Select: Estado (ACTIVO, PAUSADO, ELIMINADO)
  - FormInput: Precio base
  - Botones: Guardar / Cancelar
- ConfirmDialog para eliminar

## Estados
- [ ] Default — tabla con todos los productos
- [ ] Loading — skeleton tabla
- [ ] Modal abierto (crear) — formulario vacío
- [ ] Modal abierto (editar) — formulario pre-rellenado
- [ ] Guardando — spinner en botón del modal
- [ ] Error de validación — errores inline en formulario (nombre duplicado → 409)
- [ ] Confirm eliminar — ConfirmDialog antes de DELETE

## Notas de UX
- La constraint de unicidad es `(nombre, marcaId, categoriaId)` — el mismo nombre puede existir para distintas marcas/categorías.
- El estado ELIMINADO no elimina físicamente el registro; lo marca lógicamente.
- Al eliminar un producto, las variantes asociadas aún existen en la base de datos; manejar este caso con advertencia en el ConfirmDialog.
- Filtros en la tabla: por estado (ACTIVO/PAUSADO/ELIMINADO), por categoría, por marca.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — Product Management** for TrailForge back-office. Device: desktop layout with AdminSidebar.
>
> **Layout:**
> 1. AdminSidebar left fixed 240px (#1A1A18 bg), "Productos" item active.
> 2. Main area:
>    - Page title: "Gestión de Productos" Sora H1 + "38 productos" subtitle.
>    - Action bar: search input "Buscar por nombre..." left (50%) + filter select "Todos los estados" + "Nueva categoría" button right in #2D6A4F.
>    - **DataTable** (white surface, 8px radius, shadow): header row #F5F2EE bg, sticky. Columns: "ID" 60px | "Nombre" flex | "Marca" 120px | "Categoría" 120px | "Estado" 100px | "Precio base" 100px | "Acciones" 120px.
>      Row examples: "Bota Trail Pro 400", "Columbia", "Calzado", StatusBadge "ACTIVO" #40916C small, "$24.999", action icons: ✎ edit (gray hover green) | 🗂 variants (#2D6A4F) | 🗑 delete (hover #C1121F).
>      StatusBadge "PAUSADO" #F4A261. StatusBadge "ELIMINADO" #D4CFC8 #8C8880.
>    - Pagination row: "1-10 de 38" + prev/next buttons.
>    - **Create/Edit modal** (centered, medium shadow, 8px radius, 480px wide):
>      "Nuevo Producto" Sora H2 top + × close button. Form: Name input, Brand select dropdown, Category select dropdown, Description textarea (4 rows), Status select (ACTIVO/PAUSADO/ELIMINADO), Price base number input with "$" prefix. "Guardar" PrimaryButton + "Cancelar" SecondaryButton. Footer of modal with error: "Este nombre ya existe para esta combinación de marca y categoría." #C1121F.
>    - **Delete ConfirmDialog**: "¿Eliminar este producto?" with warning icon, "Esta acción eliminará el producto del catálogo. Sus variantes existentes permanecerán en la base de datos." "Sí, eliminar" DangerButton + "Cancelar" SecondaryButton.
