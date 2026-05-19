---
screen: Carrito
order: 4
device: responsive
status: pending
---

## Descripción
Vista del carrito de compras activo del usuario. El usuario revisa los ítems agregados, modifica cantidades, elimina productos y ve el total actualizado (con descuento si aplica) antes de iniciar el checkout.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/carritos` | Autenticado | Carrito del usuario (estado ACTIVO o VACIO) |
| `GET /api/carritos/{id}/items` | Autenticado | Items del carrito con varianteId, color, talla, productoNombre, cantidad, precioUnitario |
| `GET /api/carritos/{id}/total` | Autenticado | Monto total calculado (con descuento si aplica) |
| `PUT /api/carritos/{id}/items/{idItem}?cantidad=X` | Autenticado | Actualizar cantidad de un ítem |
| `DELETE /api/carritos/{id}/items/{idItem}` | Autenticado | Eliminar ítem del carrito |
| `POST /api/carritos/{id}/vaciar` | Autenticado | Vaciar todo el carrito |
| `POST /api/carritos/{id}/checkout` | Autenticado | Crear orden desde carrito |
| `GET /api/descuentos/activos` | Autenticado | Descuentos disponibles para aplicar |

**Datos mostrados por ítem:**
- `itemCarrito.productoNombre`
- `itemCarrito.varianteColor` + `itemCarrito.varianteTalla`
- `itemCarrito.precioUnitario` (precio al momento de agregar)
- `itemCarrito.cantidad` (editable, máx = stock disponible)
- Subtotal: `cantidad * precioUnitario`
- Foto de la variante (`GET /api/fotos/variante/{varianteId}` con orden=1)

**Resumen:**
- Subtotal de ítems
- Descuento aplicado (si `carrito.descuentoId != null`)
- Total final (`GET /api/carritos/{id}/total`)
- Estado del carrito: ACTIVO / VACIO / ABANDONADO

## Flujo de navegación
- **Viene desde:** Detalle Producto (agregar al carrito), ícono carrito en NavBar
- **Puede ir a:** Checkout Envío (botón "Comprar"), Catálogo (botón "Seguir comprando"), Detalle Producto (click en nombre de producto)
- **Trigger:** Click en ícono de carrito en NavBar; redirección automática tras agregar ítem

## Componentes UI
- NavBar (con counter actualizado)
- StepIndicator (paso 1 de 4: Carrito)
- Lista de CartItem (imagen, nombre, color/talla, QuantityInput, precio unitario, subtotal, botón eliminar)
- Botón "Vaciar carrito" (DangerButton, con ConfirmDialog)
- Panel de resumen (sticky en desktop, abajo en mobile):
  - Subtotal por ítems
  - Línea de descuento (si aplica: "-$X" o "-X%")
  - Total con descuento
  - PrimaryButton "Ir al checkout"
  - SecondaryButton "Seguir comprando"
- Toast de confirmación al eliminar ítem

## Estados
- [ ] Default — lista de ítems cargada
- [ ] Loading / skeleton — SkeletonCard mientras carga
- [ ] Empty state (VACIO) — ilustración + "Tu carrito está vacío" + botón "Explorar productos"
- [ ] Actualización de cantidad — spinner en ítem mientras se procesa PUT
- [ ] Con descuento — muestra línea de descuento en resumen
- [ ] Sin stock al actualizar — toast de error "No hay suficiente stock disponible"
- [ ] Error de checkout — toast error con mensaje descriptivo

## Notas de UX
- El backend solo permite un carrito ACTIVO/VACIO por usuario. No hay concepto de "múltiples carritos".
- Al eliminar el último ítem, el estado del carrito cambia a VACIO automáticamente.
- El precio unitario en el ítem es el precio al momento de agregar (`precioUnitario`), no el precio actual de la variante. Esto es por diseño del backend.
- El total se obtiene de `GET /api/carritos/{id}/total` que ya aplica el descuento asociado al carrito.
- El checkout (`POST /api/carritos/{id}/checkout`) valida stock nuevamente antes de crear la orden; si algún ítem ya no tiene stock, devuelve error 400.
- El carrito puede quedar en estado ABANDONADO si el usuario no actúa por 7+ días (job semanal). En ese caso mostrar banner de alerta.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Shopping Cart** page for TrailForge. Device: mobile-first, responsive.
>
> **Layout (mobile):**
> 1. NavBar fixed.
> 2. StepIndicator: 4 steps "Carrito → Envío → Pago → Confirmación", step 1 active in #2D6A4F, rest in #D4CFC8.
> 3. Page title: "Mi Carrito (3 productos)" Sora H2 #1A1A18.
> 4. **Cart items list** (white surface cards, 8px radius, 16px padding, 8px gap between items):
>    Each CartItem row: left square image 72×72px (product photo), right side: product name "Bota Trail Pro 400" Sora H3 #1A1A18, variant info "Color: Verde Oliva | Talla: 40" Inter caption #5C5C56, price row: QuantityInput (− [2] + in #D4CFC8 border 6px radius) + subtotal "$49.998" Inter label 500. Trash icon button top-right #C1121F.
>    Show 3 items total.
> 5. "Vaciar carrito" link-style button in #C1121F, text only, Inter label, aligned right.
> 6. **Order summary card** (white surface, shadow, fixed bottom on mobile, sticky sidebar on desktop):
>    - "Resumen de compra" Sora H3
>    - Row: "Subtotal (3 ítems)" + "$74.997" in #5C5C56
>    - Row: "Descuento aplicado" + "−$7.500" in #40916C (shown when discount applied)
>    - Divider #D4CFC8
>    - Row: "Total" bold + "$67.497" Sora H2 #1A1A18
>    - PrimaryButton full-width "Ir al checkout →" #2D6A4F
>    - SecondaryButton outline "Seguir comprando" below
> 7. Empty state variant: centered mountain illustration, "Tu carrito está vacío" Sora H2, "Explorá nuestros productos y encontrá el equipo perfecto para tu aventura" Inter body #5C5C56, "Explorar catálogo" PrimaryButton.
>
> **Desktop layout:** Cart items left column (65%), order summary sticky right column (35%).
