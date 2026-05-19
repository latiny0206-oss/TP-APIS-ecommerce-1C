---
screen: Checkout — Pago
order: 6
device: responsive
status: pending
---

## Descripción
Paso 3 del flujo de checkout. El usuario ingresa los datos de pago o selecciona el método. Al confirmar, se ejecuta el `POST /api/carritos/{id}/checkout` que crea la orden, descuenta stock y devuelve la OrdenResponse.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `POST /api/carritos/{id}/checkout` | Autenticado | **Acción principal**: crea la Orden, devuelve OrdenResponse |
| `GET /api/carritos/{id}/total` | Autenticado | Total final para mostrar en sidebar |
| `GET /api/carritos/{id}/items` | Autenticado | Items para resumen lateral |
| `GET /api/descuentos/activos` | Autenticado | Lista de descuentos activos disponibles para aplicar |
| `PUT /api/carritos/{id}` | Autenticado | Asociar descuento al carrito (body: descuentoId) |

**Respuesta del checkout exitoso (OrdenResponse):**
- `id`: id de la orden creada
- `estado`: PENDIENTE
- `montoFinal`: monto total con descuento aplicado
- `fechaCreacion`: timestamp de la orden
- `items`: list de ItemOrdenResponse

**Posibles errores del endpoint:**
- 400 Bad Request — "El carrito no tiene ítems" / "No hay suficiente stock"
- 401 Unauthorized — sesión expirada
- 404 Not Found — carrito no encontrado

## Flujo de navegación
- **Viene desde:** Checkout Envío (formulario de envío completado)
- **Puede ir a:** Confirmación de Orden (checkout exitoso → redirect con ordenId), Checkout Envío (botón "Volver"), Error (si checkout falla)
- **Trigger:** Botón "Confirmar compra" → `POST /api/carritos/{id}/checkout`

## Componentes UI
- NavBar
- StepIndicator (paso 3 de 4: Pago activo)
- Resumen de datos de envío (read-only, con link "Editar")
- Sección de selección de método de pago (UI ilustrativa — tarjeta crédito/débito, transferencia)
- Formulario de tarjeta de crédito (campos de UI: número, titular, vencimiento, CVV — no procesado por el backend actual)
- Campo de código de descuento (busca en `GET /api/descuentos/activos` y aplica con `PUT /api/carritos/{id}`)
- Panel de resumen del pedido (items + descuento + total final)
- PrimaryButton "Confirmar compra" (dispara el checkout)
- SecondaryButton "Volver"
- Spinner/loading overlay mientras se procesa el checkout

## Estados
- [ ] Default — formulario de pago vacío, resumen de envío visible
- [ ] Loading — "Procesando tu compra..." overlay con spinner sobre el botón de confirmar
- [ ] Con descuento aplicado — campo de código verde con check, línea de descuento en resumen
- [ ] Código de descuento inválido — mensaje de error bajo el campo
- [ ] Error de stock — toast de error "Uno de tus productos ya no tiene stock suficiente"
- [ ] Error de sesión — redirect a Login con mensaje "Tu sesión expiró"
- [ ] Checkout exitoso — redirect automático a Confirmación de Orden

## Notas de UX
- El backend ejecuta el checkout en un único endpoint `POST /api/carritos/{id}/checkout`. No hay procesamiento de pago real; el sistema es académico.
- El campo de código de descuento busca un descuento activo por nombre (o id) y lo asocia al carrito antes del checkout. La lógica: llamar `PUT /api/carritos/{id}` con `descuentoId`, luego el total se recalcula automáticamente.
- El botón "Confirmar compra" debe deshabilitar el doble-click (solo un request).
- Mostrar claramente el monto final antes del click de confirmación.
- Si el checkout falla por falta de stock, mostrar qué ítem específico causó el error y ofrecer volver al carrito.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Checkout Step 3 — Payment** for TrailForge. Device: mobile-first, responsive.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. StepIndicator: step 3 "Pago" active, steps 1 and 2 completed with checkmarks.
> 3. **Shipping summary card** (white surface, 8px radius, 16px padding): "Envío a:" label, "Juan García — Av. Libertador 1234, CABA" with edit pencil icon link in #2D6A4F.
> 4. **Payment method card**:
>    - "Método de pago" Sora H2
>    - Three large radio option cards: (a) "💳 Tarjeta de crédito/débito" selected with #2D6A4F border; (b) "🏦 Transferencia bancaria"; (c) "💵 Pago en efectivo"
>    - Credit card form below (shows when option a selected): 4 FormInputs: "Número de tarjeta" (masked "•••• •••• •••• 4242"), "Titular de la tarjeta", 2-col row ("MM/AA" + "CVV"). Card brand logos (Visa/Mastercard) appear right-aligned in the number field.
> 5. **Discount code card**:
>    - "Código de descuento" Sora H3
>    - Input with "TREK2024" placeholder + "Aplicar" SecondaryButton
>    - Success state: green check, "Descuento aplicado: −$7.500 (VERANO20 — 15%)" in #40916C
>    - Error state: "Código inválido o expirado" in #C1121F
> 6. **Order summary card**: mini list of 3 items (image 48px + name + qty), divider, rows for Subtotal / Descuento (−$7.500 in #40916C) / Envío ($1.500) / **Total $61.997** in Sora H2 #1A1A18.
> 7. "Tu compra está protegida 🔒" Inter caption #5C5C56 centered.
> 8. PrimaryButton full-width "Confirmar compra — $61.997" #2D6A4F, large 52px height.
>    Loading state: spinner inside button, text "Procesando..." with button disabled.
>
> **Desktop:** Left form 60% + right sticky summary sidebar 40%.
