---
screen: Mis Órdenes
order: 11
device: responsive
status: pending
---

## Descripción
Historial de órdenes del usuario autenticado. El usuario puede ver todas sus compras, su estado actual y acceder al detalle de cada una.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/ordenes` | Autenticado | Lista de órdenes del usuario (filtrada automáticamente si no es ADMIN) |
| `GET /api/ordenes/usuario/{idUsuario}` | Autenticado (ownership check) | Órdenes de un usuario específico |

**Datos por OrdenResponse:**
- `orden.id`
- `orden.estado` — PENDIENTE / CONFIRMADA / ENTREGADA / CANCELADA
- `orden.fechaCreacion`
- `orden.montoFinal`
- `orden.items` — lista de items (para mostrar imagen del primer ítem y count)

**Posibles estados de orden y sus colores:**
- PENDIENTE → `--color-warning` (#F4A261)
- CONFIRMADA → `--color-success` (#40916C)
- ENTREGADA → `--color-primary` (#2D6A4F)
- CANCELADA → `--color-neutral-600` (#8C8880)

## Flujo de navegación
- **Viene desde:** Perfil (link "Mis órdenes"), NavBar (si hay acceso directo), Confirmación de Orden (botón "Ver mis órdenes")
- **Puede ir a:** Detalle de Orden (click en fila de orden), Catálogo (si lista vacía)
- **Trigger:** Click en ítem de la lista de órdenes

## Componentes UI
- NavBar
- Breadcrumb ("Inicio > Mi cuenta > Mis órdenes")
- Lista de OrderSummaryCard (por orden):
  - Número de orden (#ID)
  - StatusBadge (color según estado)
  - Fecha de creación
  - Monto final
  - Imágenes en strip (thumbnails de los primeros 3 ítems)
  - Cantidad de ítems total
  - Botón/chevron "Ver detalle"
- EmptyState si no hay órdenes

## Estados
- [ ] Default — lista de órdenes ordenada por fecha desc
- [ ] Loading / skeleton — SkeletonCard × 3 mientras carga
- [ ] Empty state — "Todavía no hiciste ninguna compra" + botón "Explorar catálogo"
- [ ] Error — fallo de red, toast + retry
- [ ] Filtro por estado — tabs o chips: Todas / Pendiente / Confirmada / Entregada / Cancelada

## Notas de UX
- El backend aplica ownership automáticamente: un CLIENTE solo ve sus propias órdenes; un ADMIN ve todas.
- Ordenar por `fechaCreacion` descendente (más reciente primero).
- Las órdenes con estado PENDIENTE deberían tener un CTA directo "Confirmar" en la card (llama a `POST /api/ordenes/{id}/confirmar`).
- Las órdenes CANCELADAS pueden mostrarse más atenuadas (reduced opacity).

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **My Orders** list page for TrailForge. Device: mobile-first, responsive.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. Breadcrumb: "Inicio / Mis órdenes" Inter caption #5C5C56.
> 3. "Mis Órdenes" Sora H1 #1A1A18 + subtitle "3 órdenes registradas" Inter body #5C5C56.
> 4. Filter tabs row (horizontal scroll): "Todas" (active, #2D6A4F underline), "Pendientes", "Confirmadas", "Entregadas", "Canceladas". Inter label #5C5C56 inactive, #1A1A18 active.
> 5. **Order cards list** (vertical, 12px gap):
>    Each OrderSummaryCard (white surface, 8px radius, light shadow, 16px padding):
>    - Top row: "Orden #1042" Sora H3 #1A1A18 left + StatusBadge right. StatusBadge examples: "PENDIENTE" #F4A261 bg #1A1A18 text | "CONFIRMADA" #40916C bg white text | "ENTREGADA" #2D6A4F bg white text | "CANCELADA" #D4CFC8 bg #8C8880 text. 20px radius pill.
>    - Date row: "18 may. 2026, 14:32" Inter caption #5C5C56.
>    - Items strip: 3 small images 48×48px rounded 6px with 4px gap, then "+2 más" if more items.
>    - Bottom row: "Total: $67.497" Inter label 500 #1A1A18 left + "Ver detalle →" link-button #2D6A4F right.
>    - If PENDIENTE: show "Confirmar orden" SecondaryButton full-width below the bottom row.
>    - CANCELADA card: reduced opacity 70%.
>
>    Show 3 orders in different states for the design.
>
> 6. Empty state: compass illustration, "Todavía no hiciste ninguna compra", "Explorá nuestro catálogo y encontrá el equipo ideal." Inter body #5C5C56, "Explorar catálogo" PrimaryButton #2D6A4F.
>
> **Desktop:** Same structure but wider cards, 2-column grid optional.
