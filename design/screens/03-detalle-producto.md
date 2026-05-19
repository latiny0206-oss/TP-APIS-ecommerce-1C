---
screen: Detalle de Producto
order: 3
device: responsive
status: pending
---

## Descripción
Pantalla donde el usuario evalúa un producto específico, selecciona una variante (color + talla) y decide agregarlo al carrito. Es el punto de decisión de compra más crítico del flujo.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/productos/{id}` | Público | Nombre, descripción, marca, categoría, precioBase, estado |
| `GET /api/variantes` | Público | Variantes del producto: color, talla, material, peso, precio, stock, estación |
| `GET /api/fotos/variante/{varianteId}` | Público | Fotos ordenadas por campo `orden` (galería) |
| `GET /api/variantes/{id}/stock/disponible?cantidad=X` | Público | Verificar disponibilidad de cantidad específica |
| `GET /api/variantes/{id}/precio` | Público | Precio actual de la variante seleccionada |
| `GET /api/productos/{id}/disponible` | Público | Boolean de disponibilidad general del producto |
| `GET /api/descuentos/activos` | Autenticado | Descuentos activos (para mostrar si aplica al precio) |

**Datos mostrados:**
- Nombre del producto (H1), nombre de marca (link), nombre de categoría (breadcrumb)
- Galería de fotos ordenadas por `foto.orden`
- Precio de la variante seleccionada (`variante.precio`)
- Selectores de color (chips con el nombre del color, uno por color único entre variantes del producto)
- Selectores de talla (botones, deshabilitados si la combinación color+talla no tiene stock)
- Material, peso, estación de la variante seleccionada
- Indicador de stock: verde (>5), ámbar (1-5), rojo (0)
- Descripción del producto
- Selector de cantidad (máx = stock disponible)

## Flujo de navegación
- **Viene desde:** Catálogo (click en ProductCard), Home (click en card de novedades)
- **Puede ir a:** Carrito (click "Agregar al carrito"), Catálogo (breadcrumb), Login (si intenta agregar sin autenticar)
- **Trigger:** Click en ProductCard desde catálogo o home

## Componentes UI
- NavBar
- Breadcrumb ("Inicio > Catálogo > [Categoría] > [Nombre Producto]")
- ImageGallery (foto principal grande + strip de thumbnails, ordenadas por `foto.orden`)
- Bloque de información: nombre (H1), marca (link), categoría (badge)
- PriceDisplay (precio con/sin descuento)
- VariantSelector — Color: chips con nombre del color, se resalta el seleccionado
- VariantSelector — Talla: botones cuadrados, disabled si sin stock para el color elegido
- StockBadge (verde/ámbar/rojo según cantidad en stock)
- SeasonBadge (PRIMAVERA/VERANO/OTOÑO/INVIERNO)
- QuantityInput (botones +/- con máximo = stock)
- PrimaryButton "Agregar al carrito"
- Ficha técnica: material, peso, estación
- Descripción del producto (texto expandible en mobile)

## Estados
- [ ] Default — variante inicial seleccionada (primera disponible), galería con foto 1
- [ ] Loading / skeleton — esqueleto de galería + bloque de info
- [ ] Sin stock — VariantSelector con todas las tallas disabled, botón de compra disabled con texto "Sin stock"
- [ ] Variante sin stock — talla específica deshabilitada, StockBadge rojo
- [ ] Stock bajo — StockBadge ámbar "¡Últimas X unidades!"
- [ ] Error — producto no encontrado (redirigir a 404)
- [ ] Usuario no autenticado — al presionar "Agregar" muestra toast "Iniciá sesión para comprar" con link a Login

## Notas de UX
- La lógica de variantes es central: el producto tiene múltiples variantes (color + talla). Cuando el usuario elige un color, solo deben habilitarse las tallas disponibles para ese color. La disponibilidad se determina por `variante.stock > 0` para la combinación específica.
- El precio mostrado es `variante.precio` (no `producto.precioBase`), ya que cada variante puede tener precio distinto.
- Las fotos están asociadas a la variante, no al producto. Al cambiar de color/variante, la galería debe actualizarse con las fotos de esa variante.
- El campo `foto.orden` determina el orden de aparición en la galería.
- El selector de cantidad debe validar contra el stock real (`GET /api/variantes/{id}/stock/disponible?cantidad=X`).
- `material` y `peso` son datos de la variante, relevantes para decisión de compra en trekking.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Product Detail** page for TrailForge. Device: mobile-first, responsive.
>
> **Mobile layout (top to bottom):**
> 1. NavBar fixed.
> 2. Breadcrumb: "Inicio / Calzado / Bota Trail Pro 400" in Inter caption #5C5C56.
> 3. **Image gallery** — full-width hero image (16:9 or square, white background). Below: horizontal strip of 4 small thumbnails (72×72px, #D4CFC8 border, selected has #2D6A4F border). Images served from `/api/fotos/variante/{id}` sorted by `orden`.
> 4. **Product info block** (white surface card, 8px radius, 16px padding):
>    - Brand name: "Columbia" in Inter caption #5C5C56 uppercase
>    - Product name: "Bota Trail Pro 400" in Sora H1 #1A1A18
>    - Category badge: pill "Calzado" #2D6A4F background white text
>    - Season badge: "❄ Invierno" small pill #D4CFC8 background #5C5C56 text
>    - Price row: "$24.999" in Sora H2 #1A1A18, or if discounted: crossed "$29.999" in #8C8880 + "$24.999" in #E76F51
>    - Stock badge: "✓ En stock (8 unidades)" in #40916C, or "⚠ Últimas 3 unidades" in #F4A261
> 5. **Color selector** — Label "Color:" Inter label #5C5C56. Row of color name chips: "Verde Oliva" (selected, #2D6A4F border 2px), "Negro" (unselected #D4CFC8 border), "Azul marino". 20px border-radius chips.
> 6. **Size selector** — Label "Talla:" Inter label #5C5C56. Grid of square size buttons: "37", "38" (disabled, gray #D4CFC8 bg, crossed style), "39" (selected, #2D6A4F bg white text), "40", "41", "42". 6px radius.
> 7. **Quantity + Add to cart** — row: QuantityInput (− [2] +) with #D4CFC8 borders, then "Agregar al carrito" PrimaryButton full-width #2D6A4F. Below: Inter caption "El precio puede variar al momento del checkout."
> 8. **Specs accordion** — collapsible sections: "Detalles técnicos" (Material: Gore-Tex, Peso: 480g, Estación: Invierno), "Descripción" (product description text).
>
> **Desktop layout:** Split 55%/45% — left gallery (large image + vertical thumbnail strip), right info panel (all info block, selectors, CTA). Sticky right panel on scroll.
>
> Show the "out of stock" variant: all size buttons grayed out, CTA button says "Sin stock" disabled state (#8C8880 background).
