---
screen: Checkout — Envío
order: 5
device: responsive
status: pending
---

## Descripción
Paso 2 del flujo de checkout. El usuario ingresa los datos de entrega. Esta pantalla captura información necesaria para procesar la orden. El backend actual no tiene una entidad de dirección de envío; los datos se capturan como contexto antes del pago.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/carritos/{id}` | Autenticado | Resumen del carrito (items, total) para sidebar |
| `GET /api/carritos/{id}/items` | Autenticado | Items para mostrar en resumen lateral |
| `GET /api/carritos/{id}/total` | Autenticado | Total del carrito con descuento |

> **Nota:** El backend actual no tiene endpoint de dirección de envío ni métodos de entrega. Esta pantalla recopila datos que el frontend mantendrá en estado local hasta el paso de confirmación. La información de envío sería incluida en una futura extensión del backend.

**Datos mostrados en sidebar:**
- Resumen de ítems del carrito (imagen, nombre, cantidad, precio)
- Total de la orden

## Flujo de navegación
- **Viene desde:** Carrito (botón "Ir al checkout")
- **Puede ir a:** Checkout Pago (formulario válido + botón "Continuar"), Carrito (botón "Volver")
- **Trigger:** Submit del formulario de envío con datos válidos

## Componentes UI
- NavBar
- StepIndicator (paso 2 de 4: Envío activo)
- Formulario de datos de contacto/envío:
  - FormInput: Nombre completo
  - FormInput: Email (pre-rellenado desde perfil de usuario)
  - FormInput: Teléfono
  - FormInput: Calle y número
  - FormInput: Ciudad
  - FormInput: Provincia
  - FormInput: Código postal
- Sección "Método de envío" (opciones radio: Retiro en sucursal / Envío a domicilio — UI ilustrativa, sin endpoint)
- Panel de resumen lateral (sticky, igual que en Carrito)
- PrimaryButton "Continuar al pago"
- SecondaryButton "Volver al carrito"

## Estados
- [ ] Default — formulario vacío o pre-rellenado con datos del perfil
- [ ] Validación — errores inline en campos vacíos/inválidos (borde rojo, mensaje debajo)
- [ ] Pre-rellenado — email y nombre tomados de los datos del usuario autenticado
- [ ] Error de conexión — toast de error si falla la carga del carrito en sidebar

## Notas de UX
- El backend no tiene entidad de dirección ni envío; esta pantalla es UI-side. Los datos se almacenan en el estado del cliente y se usarán solo para mostrar en la confirmación.
- El email debe pre-rellenarse desde el perfil del usuario autenticado para reducir fricción.
- Los campos deben tener validación inline (no solo al submit): mostrar ícono de check verde cuando el campo es válido.
- Mobile: el resumen del pedido va al final de la página, colapsable. Desktop: sidebar sticky a la derecha.
- El código postal debe tener un máximo de 8 caracteres numéricos.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Checkout Step 2 — Shipping Information** for TrailForge. Device: mobile-first, responsive.
>
> **Layout (mobile, top to bottom):**
> 1. NavBar fixed.
> 2. StepIndicator: "Carrito → Envío → Pago → Confirmación", step 2 "Envío" active in #2D6A4F, steps 1 completed (checkmark), 3-4 inactive #D4CFC8.
> 3. Section card (white surface #FFFFFF, 8px radius, 24px padding):
>    - "Datos de contacto" Sora H2 #1A1A18
>    - FormInput "Nombre completo" — label Inter label 14px #5C5C56, input with #D4CFC8 border 6px radius, focused border #2D6A4F 2px, placeholder "Juan García"
>    - FormInput "Email" — pre-filled "juan@gmail.com", valid state: right-side green checkmark icon #40916C
>    - FormInput "Teléfono" — placeholder "+54 11 ..."
> 4. Section card:
>    - "Dirección de entrega" Sora H2
>    - FormInput "Calle y número" — placeholder "Av. Libertador 1234"
>    - 2-column row: FormInput "Ciudad" + FormInput "Código postal"
>    - FormInput "Provincia" — dropdown select style
>    - Error state example: one field with red #C1121F border + "Este campo es requerido" in Inter caption #C1121F below.
> 5. Section card:
>    - "Método de envío" Sora H2
>    - Two radio options as large clickable cards: (a) "📍 Retiro en sucursal — Gratis" with subtitle "3 a 5 días hábiles", selected with #2D6A4F border 2px; (b) "🏠 Envío a domicilio — $1.500" with subtitle "5 a 7 días hábiles"
> 6. Collapsible "Resumen de pedido" accordion — tap to expand, shows mini item list + total.
> 7. Bottom sticky bar: "Total: $67.497" Inter label + PrimaryButton "Continuar al pago →" full-width #2D6A4F.
>
> **Desktop:** Left form area 60% + right sticky order summary sidebar 40%.
