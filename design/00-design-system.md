# Design System — TrailForge

---

## Identidad visual

### Nombre de marca
**TrailForge**
> *Trail* (sendero) + *Forge* (forjar). Evoca la idea de construir tu camino, de equiparte con herramientas forjadas para resistir. Simple, global, memorable.

### Estilo en 3 adjetivos

| Adjetivo | Justificación |
|----------|---------------|
| **Robusto** | El rubro trekking exige equipamiento que resista. La UI debe transmitir solidez: bordes definidos, tipografía fuerte, contrastes altos. |
| **Funcional** | El usuario viene a comprar, no a contemplar. Jerarquía visual clara, flujos de compra sin fricciones, información de stock y tallas siempre visible. |
| **Aventurero** | El color, las imágenes y el tono de voz deben evocar naturaleza, altura y movimiento. No minimalismo frío sino calidez terrenal con energía. |

---

## Paleta de colores

| Token | Hex | Uso |
|-------|-----|-----|
| `--color-primary` | `#2D6A4F` | Verde bosque oscuro. Botones CTA principales, links activos, estado disponible. |
| `--color-secondary` | `#E76F51` | Naranja montaña. Badges de oferta, highlights de precio con descuento, acentos de acción secundaria. |
| `--color-bg` | `#F5F2EE` | Crema natural. Fondo global de la app, evoca papel craft y tierra. |
| `--color-surface` | `#FFFFFF` | Blanco puro. Cards de producto, modales, paneles, formularios. |
| `--color-text-primary` | `#1A1A18` | Casi negro cálido. Títulos, precios, datos críticos. |
| `--color-text-secondary` | `#5C5C56` | Gris terroso. Subtítulos, descripciones, metadatos secundarios. |
| `--color-success` | `#40916C` | Verde claro. Stock disponible, orden confirmada, validaciones OK. |
| `--color-warning` | `#F4A261` | Ámbar. Stock bajo (< 5 unidades), descuentos próximos a vencer, carritos abandonados. |
| `--color-error` | `#C1121F` | Rojo alpino. Errores de formulario, stock agotado, cancelaciones, 404. |
| `--color-neutral-100` | `#F5F2EE` | Fondo base (igual que bg). Separadores muy sutiles. |
| `--color-neutral-300` | `#D4CFC8` | Bordes de inputs, divisores de secciones, placeholders. |
| `--color-neutral-600` | `#8C8880` | Iconos inactivos, texto deshabilitado, labels de input. |
| `--color-neutral-900` | `#1A1A18` | Igual que text-primary. Negro cálido para máxima legibilidad. |

---

## Tipografía

| Nivel | Font | Tamaño | Peso | Uso |
|-------|------|--------|------|-----|
| H1 | Sora | 32px / 2rem | 700 | Títulos de página (nombre de producto, bienvenida en home) |
| H2 | Sora | 24px / 1.5rem | 600 | Títulos de sección (Categorías, Órdenes recientes) |
| H3 | Sora | 18px / 1.125rem | 600 | Subtítulos de card, nombres de variantes |
| Body | Inter | 16px / 1rem | 400 | Descripciones, contenido general, items de lista |
| Caption | Inter | 12px / 0.75rem | 400 | Metadatos, fechas, tallas/colores en badges |
| Label | Inter | 14px / 0.875rem | 500 | Labels de formulario, botones, navegación, precios |

> **Stack de fuentes:** `font-family: 'Sora', 'Inter', system-ui, sans-serif;`
> Importar de Google Fonts: Sora (weights 600, 700) + Inter (weights 400, 500).

---

## Componentes reutilizables

| Componente | Descripción | Pantallas donde aparece |
|-----------|-------------|------------------------|
| **ProductCard** | Card con imagen de variante, nombre, marca, precio, badge de descuento, indicador de stock | Home, Catálogo |
| **VariantSelector** | Grupo de botones para elegir color y talla con estado disabled cuando sin stock | Detalle Producto |
| **CartItem** | Fila de item con imagen pequeña, nombre, talla/color, cantidad editable, precio, botón eliminar | Carrito |
| **OrderItem** | Fila readonly de item con imagen, nombre, cantidad, precio al momento | Detalle Orden, Confirmación |
| **PriceDisplay** | Componente de precio con tachado del precio base y precio con descuento en `--color-secondary` | Carrito, Detalle Producto, Checkout |
| **DiscountBadge** | Badge naranja con porcentaje o monto fijo de descuento | ProductCard, Carrito, Checkout |
| **StockBadge** | Badge verde (disponible), ámbar (< 5 uds), rojo (agotado) | Detalle Producto, Admin Variantes |
| **SeasonBadge** | Badge pequeño con icono y texto de estación (PRIMAVERA, VERANO, OTOÑO, INVIERNO) | Detalle Producto, Admin Variantes |
| **StatusBadge** | Badge genérico con color según estado (Orden: PENDIENTE/CONFIRMADA/ENTREGADA/CANCELADA) | Mis Órdenes, Detalle Orden, Admin |
| **NavBar** | Barra de navegación superior con logo, búsqueda, ícono de carrito (con contador), perfil | Todas las pantallas de usuario |
| **AdminSidebar** | Menú lateral con links a secciones admin | Todas las pantallas de admin |
| **Breadcrumb** | Navegación de migas de pan | Catálogo, Detalle Producto, Checkout |
| **FilterPanel** | Panel lateral/drawer con filtros de categoría, marca, precio, estación | Catálogo |
| **QuantityInput** | Input numérico con botones +/- con validación de stock máximo | Carrito, Detalle Producto |
| **EmptyState** | Ilustración + mensaje + CTA cuando no hay contenido | Carrito vacío, Sin órdenes, 404 |
| **SkeletonCard** | Placeholder animado para ProductCard durante carga | Catálogo, Home |
| **FormInput** | Input de texto con label, validación inline y mensaje de error | Login, Registro, Checkout |
| **PrimaryButton** | Botón CTA en `--color-primary` con estados hover/disabled/loading | Todas |
| **SecondaryButton** | Botón outline en `--color-primary` | Todas |
| **DangerButton** | Botón en `--color-error` para acciones destructivas | Admin, Carrito |
| **StepIndicator** | Indicador de pasos del checkout (Carrito → Envío → Pago → Confirmación) | Checkout Envío, Checkout Pago |
| **ImageGallery** | Galería de fotos de variante con thumbnail strip y zoom | Detalle Producto |
| **DataTable** | Tabla con sorting, paginación y acciones por fila | Todas las pantallas admin |
| **ConfirmDialog** | Modal de confirmación para acciones irreversibles | Admin, Cancelar orden |
| **Toast** | Notificación temporal de feedback (éxito, error, warning) | Todas las pantallas |

---

## Prompt base de estilo

> **BASE STYLE PROMPT — include verbatim in every screen prompt:**
>
> *Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.*
