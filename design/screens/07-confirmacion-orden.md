---
screen: Confirmación de Orden
order: 7
device: responsive
status: pending
---

## Descripción
Pantalla de éxito post-checkout. El usuario ve el resumen de su orden creada (estado PENDIENTE), los detalles de los ítems comprados y el monto final. Es el punto de cierre del flujo de compra.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/ordenes/{id}` | Autenticado | OrdenResponse completa (id, estado, fechaCreacion, montoFinal, items, descuentoId) |
| `GET /api/ordenes/{id}/items` | Autenticado | List<ItemOrdenResponse> con datos de cada ítem |
| `POST /api/ordenes/{id}/confirmar` | Autenticado | Cambiar estado PENDIENTE → CONFIRMADA |

**Datos de OrdenResponse:**
- `orden.id` — número de orden para referencia
- `orden.estado` — PENDIENTE (inicial tras checkout)
- `orden.fechaCreacion` — timestamp de la compra
- `orden.montoFinal` — monto total pagado

**Datos de ItemOrdenResponse (por ítem):**
- `productoNombre`
- `varianteColor` + `varianteTalla`
- `cantidad`
- `precioAlMomento` — precio histórico al momento de la compra

## Flujo de navegación
- **Viene desde:** Checkout Pago (automático tras `POST /api/carritos/{id}/checkout` exitoso)
- **Puede ir a:** Mis Órdenes, Home, Catálogo
- **Trigger:** Redirección automática con `?ordenId=X` en la URL

## Componentes UI
- NavBar
- Ícono de éxito grande (check verde sobre fondo circular #40916C)
- Número de orden prominente ("Orden #1042")
- StatusBadge PENDIENTE (ámbar)
- Resumen de ítems (OrderItem: imagen, nombre, color/talla, cantidad, precio al momento)
- Desglose de totales (subtotal, descuento, total final)
- Datos de envío confirmados (nombre, dirección — desde estado local del checkout)
- Fecha y hora de la orden
- PrimaryButton "Confirmar orden" → `POST /api/ordenes/{id}/confirmar` (cambia a CONFIRMADA)
- SecondaryButton "Ver mis órdenes"
- SecondaryButton "Seguir comprando"

## Estados
- [ ] Default — orden creada, estado PENDIENTE, esperando confirmación del usuario
- [ ] Confirmando — spinner en botón "Confirmar orden"
- [ ] Orden confirmada — StatusBadge cambia a CONFIRMADA (verde), botón desaparece
- [ ] Error al cargar — si el fetch de la orden falla (toast + retry)

## Notas de UX
- El `precioAlMomento` en cada ItemOrden es el precio histórico capturado en el momento del checkout. Esto garantiza que si el precio de una variante cambia en el futuro, la orden conserva el precio original.
- La pantalla tiene dos momentos: (1) confirmación visual de éxito del checkout y (2) acción de "Confirmar orden" que ejecuta la transición de estado en el backend.
- El número de orden debe ser el campo `id` del OrdenResponse.
- Enviar email de confirmación es una funcionalidad fuera del scope del backend actual — no representar.
- Mobile: el check de éxito y el número de orden deben ser lo primero visible sin scroll.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Order Confirmation** page for TrailForge, shown immediately after a successful checkout. Device: mobile-first, responsive.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. **Success hero block** (full-width, background gradient from #2D6A4F to #40916C, white text, 48px padding):
>    - Large animated checkmark icon in white circle (80×80px)
>    - "¡Compra realizada!" Sora H1 white
>    - "Te confirmamos tu pedido. En breve lo revisaremos." Inter body white 80% opacity
>    - "Orden #1042" Inter label 500 white, bold background pill
> 3. **Order status card** (white surface, 8px radius, shadow):
>    - Row: "Estado" label + StatusBadge "PENDIENTE" in #F4A261 background, #1A1A18 text
>    - Row: "Fecha" label + "18 de mayo de 2026, 14:32" Inter body
>    - CTA: "Confirmar orden" PrimaryButton #2D6A4F (triggers state change to CONFIRMADA)
>    - Note below: "Al confirmar, se acreditará tu compra." Inter caption #5C5C56
> 4. **Items ordered card**:
>    - "Productos comprados" Sora H2
>    - List of OrderItems: image 64×64px square, product name Sora H3, "Verde Oliva — T.40 — ×2" Inter caption #5C5C56, price "$49.998" Inter label 500 right-aligned.
>    - Divider
>    - Totals: "Subtotal" + "$74.997", "Descuento (VERANO20)" + "−$7.500" in #40916C, "Total pagado" bold + "$67.497" Sora H2 #1A1A18
> 5. **Shipping info card** (read-only):
>    - "Envío a" Sora H3
>    - "Juan García — Av. Libertador 1234, CABA (1001)"
>    - "Método: Envío a domicilio" Inter caption
> 6. Two buttons: "Ver mis órdenes" SecondaryButton + "Seguir comprando" link-style in #2D6A4F.
>
> Emotional tone: relief, celebration, confidence. Like reaching the summit — mission accomplished.
