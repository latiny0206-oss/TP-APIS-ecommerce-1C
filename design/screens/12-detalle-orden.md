---
screen: Detalle de Orden
order: 12
device: responsive
status: pending
---

## Descripción
Vista completa de una orden específica. El usuario ve todos los ítems comprados con precios al momento de la compra, el desglose del total, el estado actual y puede confirmar o cancelar la orden según su estado.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/ordenes/{id}` | Autenticado (ownership) | OrdenResponse completa |
| `GET /api/ordenes/{id}/items` | Autenticado | List<ItemOrdenResponse> |
| `GET /api/ordenes/{id}/monto-final` | Autenticado | BigDecimal monto final |
| `POST /api/ordenes/{id}/confirmar` | Autenticado | Cambiar estado: PENDIENTE → CONFIRMADA |
| `POST /api/ordenes/{id}/cancelar` | Autenticado | Cambiar estado a CANCELADA |

**Datos de OrdenResponse:**
- `id`, `estado`, `fechaCreacion`, `montoFinal`
- `descuentoId` (si hubo descuento aplicado)
- `carritoId` (referencia al carrito origen)

**Datos de ItemOrdenResponse:**
- `productoNombre`, `varianteColor`, `varianteTalla`
- `cantidad`
- `precioAlMomento` — precio histórico (no el precio actual de la variante)
- `varianteId` — para cargar la foto

**Estados de orden y acciones disponibles:**
- PENDIENTE → puede Confirmar o Cancelar
- CONFIRMADA → solo puede Cancelar
- ENTREGADA → sin acciones disponibles
- CANCELADA → sin acciones disponibles

## Flujo de navegación
- **Viene desde:** Mis Órdenes (click en "Ver detalle"), Confirmación de Orden
- **Puede ir a:** Mis Órdenes (breadcrumb / botón volver), Catálogo (CTA si cancelada)
- **Trigger:** Click en "Ver detalle" desde la lista de órdenes

## Componentes UI
- NavBar
- Breadcrumb ("Inicio > Mis órdenes > Orden #1042")
- Encabezado: número de orden, StatusBadge, fecha
- Lista de OrderItem (imagen, nombre, color/talla, cantidad, precioAlMomento, subtotal)
- Desglose de totales (subtotal de ítems, descuento si aplica, total final)
- Información de envío (read-only, si fue capturada)
- Bloque de acciones según estado:
  - PENDIENTE: PrimaryButton "Confirmar orden" + DangerButton "Cancelar orden"
  - CONFIRMADA: DangerButton "Cancelar orden" (con ConfirmDialog)
  - ENTREGADA / CANCELADA: sin acciones (badge informativo)
- ConfirmDialog antes de cancelar

## Estados
- [ ] Default — orden cargada, acciones según estado
- [ ] Loading / skeleton — esqueleto de la orden
- [ ] Confirmando — spinner en botón "Confirmar"
- [ ] Cancelando — ConfirmDialog abierto → spinner → estado actualizado
- [ ] Orden confirmada — StatusBadge actualizado a CONFIRMADA en verde, botón "Confirmar" desaparece
- [ ] Orden cancelada — StatusBadge CANCELADA gris, card tenue
- [ ] Error — toast de error si falla la acción

## Notas de UX
- El `precioAlMomento` es un snapshot del precio al momento de la compra; puede diferir del precio actual de la variante. Añadir tooltip explicativo: "Precio al momento de la compra".
- La cancelación debe usar ConfirmDialog: "¿Cancelar esta orden? Esta acción no se puede deshacer."
- Si la orden tiene estado CANCELADA, el backend restaura el stock de las variantes.
- El monto mostrado debe ser el `montoFinal` de la orden (ya con descuento aplicado).

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Order Detail** page for TrailForge. Device: mobile-first, responsive. Show the PENDIENTE state.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. Breadcrumb: "Mis órdenes / Orden #1042" Inter caption #5C5C56.
> 3. **Order header card** (white surface, 8px radius):
>    - "Orden #1042" Sora H1 #1A1A18 left + StatusBadge "PENDIENTE" #F4A261 bg right.
>    - "Creada el 18 de mayo de 2026 a las 14:32" Inter body #5C5C56.
>    - Info row: "Referencia carrito: #87" Inter caption #8C8880.
> 4. **Actions card** (only visible for actionable states):
>    - "Acciones disponibles" Sora H3
>    - PrimaryButton "✓ Confirmar orden" full-width #2D6A4F (PENDIENTE → CONFIRMADA)
>    - DangerButton "✕ Cancelar orden" full-width outline #C1121F border and text, below the primary button.
>    - Note: "Podés confirmar o cancelar mientras la orden esté pendiente." Inter caption #8C8880.
> 5. **Ordered items card**:
>    - "Productos (3 ítems)" Sora H2
>    - Each OrderItem row: image 72×72px 8px radius, right side: "Bota Trail Pro 400" Sora H3 #1A1A18, "Verde Oliva — Talla 40" Inter caption #5C5C56 with tooltip "Precio al momento de la compra" on price, quantity "×2" badge, price "$49.998" Inter label 500 right-aligned.
>    - #D4CFC8 dividers between items.
> 6. **Totals card**:
>    - Row: "Subtotal (3 ítems)" #5C5C56 + "$74.997" right
>    - Row: "Descuento aplicado (VERANO20)" #40916C + "−$7.500" #40916C right
>    - Hairline divider
>    - Row: "Total pagado" Sora H2 #1A1A18 bold + "$67.497" Sora H2 #1A1A18 right bold
> 7. **Cancellation confirm dialog** (modal overlay): "¿Cancelar esta orden?" Sora H2, "Esta acción no se puede deshacer. Se restaurará el stock de los productos." Inter body, "Sí, cancelar" DangerButton + "No, volver" SecondaryButton.
