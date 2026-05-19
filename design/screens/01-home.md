---
screen: Home
order: 1
device: responsive
status: pending
---

## Descripción
Página de entrada de TrailForge. El usuario llega aquí directamente (anónimo o autenticado) y necesita orientarse rápidamente: entender la propuesta de valor, explorar categorías de productos y ver novedades o destacados que lo motiven a continuar navegando.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/categorias` | Público | Lista de categorías con id, nombre, descripción |
| `GET /api/marcas` | Público | Lista de marcas con id, nombre, descripción |
| `GET /api/productos/estado/ACTIVO` | Público | Productos activos (para sección "Novedades") |
| `GET /api/variantes` | Público | Variantes con precio, stock, estación |
| `GET /api/fotos/variante/{varianteId}` | Público | Fotos de variante (imagen principal de card) |

**Datos mostrados:**
- Categorías como accesos directos visuales (nombre + ícono/imagen)
- Grid de productos destacados: nombre, marca, precio base, badge de descuento si aplica, imagen
- Marcas como logos en carrusel horizontal

## Flujo de navegación
- **Viene desde:** Entrada directa, Logo en NavBar
- **Puede ir a:** Catálogo (click en categoría o "Ver todo"), Detalle Producto (click en card), Login/Registro (NavBar), Carrito (ícono NavBar)
- **Trigger:** Al cargar la app sin ruta específica; también desde el logo de NavBar

## Componentes UI
- NavBar (logo, buscador, ícono carrito con counter, link perfil/login)
- Hero banner con texto y CTA "Explorar catálogo"
- Grid de categorías (CategoriaCard: ícono representativo + nombre)
- Sección "Novedades" con grid de ProductCard (máx 8)
- Carrusel de marcas (logos o nombre)
- Footer con links de navegación

## Estados
- [ ] Default — categorías y productos cargados
- [ ] Loading / skeleton — SkeletonCard visible mientras carga
- [ ] Empty state — no hay productos activos (raro pero posible)
- [ ] Usuario autenticado — NavBar muestra nombre de usuario, no botón Login
- [ ] Error — fallo en fetch de productos (banner de error con retry)

## Notas de UX
- Las categorías son el principal punto de entrada al catálogo; darles protagonismo visual (iconos grandes, texto claro).
- El grid de novedades debe mostrar el precio más bajo de todas las variantes del producto (ya que cada variante tiene su propio precio).
- Si el producto tiene descuento activo disponible, mostrar `DiscountBadge` sobre la imagen.
- La búsqueda en NavBar debe llevar al catálogo con el término pre-cargado como filtro.
- Mobile: el hero ocupa 100vw, categorías en scroll horizontal, productos en grid 2 columnas.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Home page** of TrailForge trekking e-commerce store. Device: mobile-first, responsive (show mobile layout primarily, with desktop hints).
>
> Layout structure (top to bottom):
> 1. **NavBar** — fixed top bar with TrailForge logo (left), search bar (center, pill-shaped, #D4CFC8 border), cart icon with item count badge in #E76F51 (right), user avatar/login link (right). Background #FFFFFF with bottom border #D4CFC8.
> 2. **Hero section** — full-width banner with a mountain trail photo as overlay background, dark gradient overlay from bottom. Large Sora H1 headline: "Equipate para la montaña" in white. Subtext in Inter body: "Calzado, ropa y accesorios para cada estación." CTA button "Explorar catálogo" in #2D6A4F filled, rounded 6px. Minimum height 280px mobile.
> 3. **Categories grid** — section title "Explorar por categoría" in Sora H2 #1A1A18. Below: horizontal scroll row on mobile (fixed 2-column grid on desktop) of category cards. Each category card: white surface #FFFFFF, 8px border-radius, light shadow, centered category icon (use nature-themed placeholder icons), category name in Sora H3. Real category examples: Calzado, Ropa, Mochilas, Accesorios, Equipamiento.
> 4. **Featured products** — section title "Novedades" in Sora H2. 2-column grid on mobile, 4-column on desktop. Each ProductCard: white card #FFFFFF, product photo top, product name in Sora H3 #1A1A18, brand name in Inter caption #5C5C56, price in Inter label 500 weight #1A1A18. If discount: crossed-out original price + discounted price in #E76F51. DiscountBadge pill "−15%" in #E76F51 background, white text, 20px radius, top-right corner of image. Stock status: small dot indicator (green #40916C = in stock, red #C1121F = out of stock).
> 5. **Brands strip** — section "Marcas" with horizontal logo carrusel, grayscale logos that color on hover.
> 6. **Footer** — dark background #1A1A18, links in #8C8880, logo in white.
>
> Emotional tone: adventure readiness, trust, quality. Like a well-stocked basecamp store before a summit attempt.
