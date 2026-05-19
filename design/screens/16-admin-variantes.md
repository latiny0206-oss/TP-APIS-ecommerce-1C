---
screen: Admin — Gestión de Variantes y Fotos
order: 16
device: responsive
status: pending
---

## Descripción
Pantalla de administración para gestionar las variantes de un producto específico: combinaciones de color/talla, stock, precio y fotos. Es la pantalla más compleja del panel admin.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/variantes` | Público | Todas las variantes (filtradas por productoId en frontend) |
| `GET /api/variantes/{id}` | Público | Variante específica |
| `POST /api/variantes` | ADMIN | Crear variante |
| `PUT /api/variantes/{id}` | ADMIN | Editar variante |
| `DELETE /api/variantes/{id}` | ADMIN | Eliminar variante |
| `GET /api/variantes/{id}/stock/disponible?cantidad=X` | Público | Verificar stock |
| `GET /api/fotos/variante/{varianteId}` | Público | Fotos de la variante |
| `POST /api/fotos` | ADMIN | Subir foto (multipart/form-data: varianteId, orden, archivo) — max 10MB |
| `PUT /api/fotos/{id}` | ADMIN | Actualizar foto |
| `DELETE /api/fotos/{id}` | ADMIN | Eliminar foto |

**Datos de VarianteProductoRequest:**
- `productoId` (Long) — fijo (viene del producto seleccionado)
- `color` (String) — texto libre
- `talla` (String) — texto libre
- `material` (String) — texto libre
- `peso` (BigDecimal) — kg
- `stock` (Integer) — unidades
- `precio` (BigDecimal) — precio de venta
- `estacion` (Estacion) — PRIMAVERA / VERANO / OTONO / INVIERNO

**Constraint única:** (productoId, color, talla) — no puede haber dos variantes con mismo color y talla para el mismo producto.

**Upload de fotos:**
- Content-Type: multipart/form-data
- Max file size: 10MB (configurado en properties)
- Campos: varianteId, orden (Integer), archivo (MultipartFile)
- Datos almacenados: nombre, tipoContenido, orden, datos (LONGBLOB)

## Flujo de navegación
- **Viene desde:** Admin Productos (botón "Ver variantes" en fila de producto)
- **Puede ir a:** Admin Productos (breadcrumb), Admin Dashboard
- **Trigger:** Click en ícono de variantes en la tabla de productos

## Componentes UI
- AdminSidebar
- NavBar admin
- Breadcrumb ("Productos > Bota Trail Pro 400 > Variantes")
- Header con nombre del producto y botón "Nueva variante"
- DataTable de variantes: Color, Talla, Material, Peso, Stock (StockBadge), Precio, Estación (SeasonBadge), Acciones
- Panel de fotos por variante (expandible al seleccionar una variante):
  - Grid de fotos con orden
  - Botón subir foto (input file)
  - Reordenar fotos (drag or arrows)
  - Eliminar foto
- Modal "Nueva / Editar Variante" con todos los campos
- ConfirmDialog para eliminar

## Estados
- [ ] Default — tabla de variantes del producto seleccionado
- [ ] Sin variantes — EmptyState "Este producto no tiene variantes. Creá la primera."
- [ ] Modal variante — formulario crear/editar
- [ ] Panel fotos expandido — grid de fotos de la variante seleccionada
- [ ] Subiendo foto — progress bar o spinner
- [ ] Error de foto — file too large (413), wrong format
- [ ] Stock crítico — fila con StockBadge rojo destacada

## Notas de UX
- El campo `estacion` usa el enum Estacion: PRIMAVERA, VERANO, OTONO (sin tilde en el enum), INVIERNO. Mostrar con tildes en la UI.
- El `peso` está en BigDecimal con precision 8, scale 2 — representar con "kg" como sufijo.
- Las fotos tienen un campo `orden` que determina el orden de galería. Permitir reordenar con flechas arriba/abajo.
- Al eliminar la última variante de un producto, el producto queda sin stock y `estaDisponible` retorna false.
- El campo `precio` de la variante puede diferir del `precioBase` del producto. Ambos coexisten.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — Variant & Photo Management** for product "Bota Trail Pro 400" in TrailForge back-office. Desktop layout with AdminSidebar.
>
> **Layout:**
> 1. AdminSidebar left, "Productos" active.
> 2. Breadcrumb: "Productos / Bota Trail Pro 400 / Variantes" Inter caption.
> 3. Page header: "Variantes de Bota Trail Pro 400" Sora H1 + "4 variantes" subtitle + "Nueva variante" PrimaryButton right.
> 4. **Variants DataTable** (white surface, 8px radius):
>    Columns: Color | Talla | Material | Peso | Stock | Precio | Estación | Acciones.
>    Row examples:
>    - "Verde Oliva" | "39" | "Gore-Tex" | "480 g" | StockBadge "12 uds" #40916C | "$24.999" | SeasonBadge "❄ Invierno" #D4CFC8 bg | ✎ 📷 🗑
>    - "Negro" | "40" | "Gore-Tex" | "480 g" | StockBadge "2 uds" #F4A261 warning | "$24.999" | SeasonBadge | actions
>    - "Azul marino" | "42" | "Gore-Tex" | "480 g" | StockBadge "0 uds" #C1121F | "$22.999" | SeasonBadge | actions
>    Camera icon (📷) opens photo panel.
> 5. **Photo panel** (slide-in panel or expandable row below selected variant, white surface):
>    "Fotos de Verde Oliva — T.39" Sora H3. Grid 4×2 of photo cards: each 120×120px with image, "Orden: 1" caption, delete X button top-right. Last card: dashed border #D4CFC8 "+" upload button. Drag reorder handles left side (⠿ icon).
>    File upload note: "Archivos JPG/PNG, máximo 10 MB" Inter caption #8C8880.
> 6. **Variant modal** (480px centered): "Nueva Variante" Sora H2. Form: Color text input, Talla text input, Material text input, Peso number input + "kg" suffix, Stock number input (min 0), Precio number input + "$" prefix, Estación select (PRIMAVERA/VERANO/OTOÑO/INVIERNO). "Guardar" + "Cancelar". Error: "Ya existe una variante Verde Oliva — T.39 para este producto." #C1121F.
