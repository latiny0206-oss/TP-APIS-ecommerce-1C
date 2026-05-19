---
screen: Catálogo
order: 2
device: responsive
status: pending
---

## Descripción
Vista principal de exploración de productos. El usuario puede filtrar por categoría, marca y estación; ordenar resultados; y navegar a cualquier producto. Es la pantalla más utilizada del flujo de compra.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/productos` | Público | Todos los productos (nombre, marca, categoría, precioBase, estado) |
| `GET /api/productos/categoria/{categoriaId}` | Público | Productos filtrados por categoría |
| `GET /api/productos/marca/{marcaId}` | Público | Productos filtrados por marca |
| `GET /api/productos/estado/ACTIVO` | Público | Solo productos activos |
| `GET /api/categorias` | Público | Lista de categorías para filtros |
| `GET /api/marcas` | Público | Lista de marcas para filtros |
| `GET /api/variantes` | Público | Variantes con precio, stock, estación, color, talla |
| `GET /api/fotos/variante/{varianteId}` | Público | Foto principal de cada variante |

**Datos mostrados por producto:**
- Nombre del producto, nombre de marca, nombre de categoría
- Precio base (o precio mínimo de variantes)
- Imagen principal (foto con orden=1 de la primera variante)
- Indicador de disponibilidad (al menos una variante con stock > 0)
- Badge de temporada si todas las variantes son de una estación

## Flujo de navegación
- **Viene desde:** Home (click en categoría o "Ver todo"), NavBar búsqueda, Breadcrumb
- **Puede ir a:** Detalle Producto (click en card), Home (breadcrumb / logo)
- **Trigger:** Selección de categoría, búsqueda por texto, aplicar filtros

## Componentes UI
- NavBar
- Breadcrumb ("Inicio > Catálogo > [Categoría]")
- FilterPanel (sidebar en desktop, drawer bottom-sheet en mobile)
  - Filtro por Categoría (checkboxes con nombre y count)
  - Filtro por Marca (checkboxes)
  - Filtro por Estación (PRIMAVERA, VERANO, OTOÑO, INVIERNO — chips)
  - Filtro de precio (rango slider o inputs min/max)
- Barra de resultados (contador "X productos encontrados" + selector de ordenamiento)
- Grid de ProductCard (2 columnas mobile, 3-4 desktop)
- Paginación o infinite scroll

## Estados
- [ ] Default — grid de productos cargado con filtros en estado inicial
- [ ] Loading / skeleton — SkeletonCard en grid mientras carga
- [ ] Empty state — ningún producto coincide con filtros aplicados (mensaje + botón "Limpiar filtros")
- [ ] Error — fallo de red (mensaje + retry)
- [ ] Filtros aplicados — chips de filtros activos visibles sobre el grid con botón "×" para quitar
- [ ] Sin stock — ProductCard con overlay "Sin stock" y botón deshabilitado

## Notas de UX
- El backend no tiene endpoint de búsqueda por texto; la búsqueda textual debe hacerse client-side filtrando la lista ya cargada por nombre de producto.
- Solo mostrar productos con `estado = ACTIVO`. Los estados PAUSADO y ELIMINADO no deben aparecer en el catálogo público.
- El precio a mostrar en la card es el más bajo entre todas las variantes del producto (la API devuelve el `precioBase` del producto y precios individuales por variante).
- El indicador de stock se calcula verificando si alguna variante tiene `stock > 0`.
- Mobile: FilterPanel se abre desde un botón "Filtros" como bottom drawer.
- Los chips de filtro activos deben ser visibles incluso cuando el panel está cerrado.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Product Catalog** page of TrailForge. Device: mobile-first, responsive.
>
> Layout on mobile (show both mobile and desktop side-by-side in the design):
>
> **Mobile layout:**
> 1. NavBar (fixed top).
> 2. Breadcrumb row: "Inicio / Catálogo" in Inter caption #5C5C56.
> 3. Sticky filter/sort bar below breadcrumb: left side "Filtros" button with funnel icon (opens bottom drawer), right side "Ordenar" dropdown (Por relevancia, Precio: menor a mayor, Precio: mayor a menor). Background #FFFFFF, border-bottom #D4CFC8.
> 4. Active filter chips row (horizontal scroll): chips like "Calzado ×", "Columbia ×" in #2D6A4F background, white text, 20px radius. "Limpiar todo" link in #E76F51.
> 5. Results count: "24 productos encontrados" in Inter body #5C5C56.
> 6. 2-column product grid. Each ProductCard: white surface #FFFFFF, 8px radius, photo (4:3 ratio), product name Sora H3 #1A1A18 (2 lines max, ellipsis), brand in Inter caption #5C5C56, price in Inter label 500 #1A1A18. If discount badge: orange pill "−20%" top-right on image. If out of stock: "Sin stock" gray overlay on image.
>
> **Desktop layout sidebar:**
> Left sidebar 240px: "Filtros" title, collapsible sections (Categoría with checkboxes: Calzado, Ropa, Mochilas, Accesorios; Marca with checkboxes: Columbia, Montagne, Lippi; Estación with 4 season chip buttons; Precio range with two number inputs). Right area: 3-4 column grid.
>
> **Filter bottom drawer (mobile):** slides up from bottom, full height option. Contains all filter sections. "Aplicar filtros" button in #2D6A4F full-width at bottom of drawer.
>
> Show the empty state variant: centered illustration of mountain with no path, text "No encontramos productos con esos filtros", button "Limpiar filtros" in #2D6A4F outline style.
