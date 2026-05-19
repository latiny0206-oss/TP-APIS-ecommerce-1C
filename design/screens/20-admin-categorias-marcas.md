---
screen: Admin — Categorías y Marcas
order: 20
device: responsive
status: pending
---

## Descripción
Panel combinado para administrar las entidades de clasificación: Categorías (tipos de producto) y Marcas (fabricantes). Son simples listados con CRUD básico.

## Datos del backend

### Categorías
| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/categorias` | Público | Lista: id, nombre, descripción |
| `GET /api/categorias/{id}` | Público | Categoría específica |
| `POST /api/categorias` | ADMIN | Crear categoría |
| `PUT /api/categorias/{id}` | ADMIN | Editar categoría |
| `DELETE /api/categorias/{id}` | ADMIN | Eliminar categoría |

### Marcas
| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/marcas` | Público | Lista: id, nombre, descripción |
| `GET /api/marcas/{id}` | Público | Marca específica |
| `POST /api/marcas` | ADMIN | Crear marca |
| `PUT /api/marcas/{id}` | ADMIN | Editar marca |
| `DELETE /api/marcas/{id}` | ADMIN | Eliminar marca |

**Datos de CategoriaRequest / MarcaRequest:**
- `nombre` (String) — @NotBlank, max 100 chars
- `descripcion` (String) — opcional, TEXT

**Datos de CategoriaResponse / MarcaResponse:**
- `id` (Long)
- `nombre` (String)
- `descripcion` (String)

## Flujo de navegación
- **Viene desde:** AdminSidebar ("Categorías" o "Marcas"), Admin Dashboard
- **Puede ir a:** Admin Productos (para ver productos de una categoría/marca)
- **Trigger:** Navegación desde sidebar

## Componentes UI
- AdminSidebar
- NavBar admin
- Tabs o secciones: "Categorías" / "Marcas"
- Para cada sección:
  - Botón "Nueva categoría" / "Nueva marca"
  - Lista en tabla simple: ID, Nombre, Descripción, Cantidad de productos (calculado), Acciones
  - Modal inline crear/editar (nombre + descripción)
  - ConfirmDialog para eliminar (con advertencia si tiene productos asociados)

## Estados
- [ ] Default — ambas listas cargadas
- [ ] Tab Categorías activo / Tab Marcas activo
- [ ] Modal crear/editar abierto
- [ ] Confirm eliminar — con advertencia de productos asociados
- [ ] Empty state — sin categorías / sin marcas

## Notas de UX
- Al eliminar una categoría o marca que tenga productos asociados, el backend lanzará un `DataIntegrityViolationException` (409 Conflict) por foreign key constraint. Mostrar este error como "No se puede eliminar porque tiene productos asociados."
- La cantidad de productos por categoría/marca no tiene un endpoint dedicado; debe calcularse client-side cruzando los datos de `/api/productos`.
- En mobile: las dos listas se muestran en tabs verticales separadas; en desktop se pueden mostrar en dos columnas side-by-side.
- El campo `descripcion` es opcional tanto en Categoria como en Marca.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — Categories & Brands** management page for TrailForge back-office. Desktop layout with AdminSidebar. Show both sections side-by-side on desktop.
>
> **Layout (desktop, two-column after sidebar):**
> 1. AdminSidebar left, "Categorías" active.
> 2. Page header: "Categorías y Marcas" Sora H1.
> 3. **Two-column layout** (each column ~50%, white surface cards, 8px radius, shadow):
>
>    **Left column — Categorías:**
>    - "Categorías" Sora H2 + "Nueva categoría" PrimaryButton small right.
>    - Simple list/table: rows with "Calzado" Inter label + "8 productos" Inter caption #5C5C56 + ✎ 🗑 icons. Items: Calzado (8), Ropa (15), Mochilas (6), Accesorios (4), Equipamiento (5).
>    - Last row: dashed border card "+ Nueva categoría" with + icon, clickable.
>    - Inline edit form (expands below selected row): Nombre input + Descripción textarea (2 rows) + Save/Cancel buttons.
>
>    **Right column — Marcas:**
>    - "Marcas" Sora H2 + "Nueva marca" PrimaryButton small right.
>    - List: "Columbia" (12 productos), "Montagne" (8), "Lippi" (6), "Adidas Terrex" (5), "Merrell" (7).
>    - Same inline edit form pattern.
>
> 4. **Delete ConfirmDialog** (for category/brand with products): "¿Eliminar la categoría 'Calzado'? No es posible eliminar una categoría con productos asociados. Reasigná los 8 productos primero." With only "Entendido" button (no destructive action available).
>    Alternative dialog for empty category: "¿Eliminar 'Accesorios'? Esta categoría no tiene productos. La eliminación es permanente." DangerButton + Cancel.
>
> **Mobile:** Tab bar "Categorías | Marcas" switches between the two single-column lists.
