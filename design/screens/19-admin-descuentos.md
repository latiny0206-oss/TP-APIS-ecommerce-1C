---
screen: Admin — Gestión de Descuentos
order: 19
device: responsive
status: pending
---

## Descripción
Panel de administración para crear y gestionar cupones de descuento. Los descuentos pueden ser de tipo porcentaje o monto fijo, con fechas de inicio y fin. Un job automático los expira diariamente.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/descuentos` | Público | Todos los descuentos (todos los estados) |
| `GET /api/descuentos/activos` | Autenticado | Solo descuentos con estado ACTIVO |
| `GET /api/descuentos/{id}` | Público | Descuento específico |
| `POST /api/descuentos` | ADMIN | Crear descuento |
| `PUT /api/descuentos/{id}` | ADMIN | Editar descuento |
| `DELETE /api/descuentos/{id}` | ADMIN | Eliminar descuento |
| `GET /api/descuentos/{id}/vigente` | Público | Boolean — si está vigente ahora |
| `GET /api/descuentos/{id}/calcular?monto=X` | Público | Calcular valor del descuento para un monto |

**Datos de DescuentoRequest:**
- `nombre` — @NotBlank, @Size(max=100), único
- `tipo` — PORCENTAJE o FIJO
- `valor` — BigDecimal >= 0 (porcentaje como 15.00, o monto como 500.00)
- `fechaInicio` — LocalDate
- `fechaFin` — LocalDate (debe ser >= fechaInicio)
- `estado` — ACTIVO o EXPIRADO

**Datos de DescuentoResponse:**
- `id`, `nombre`, `tipo`, `valor`, `fechaInicio`, `fechaFin`, `estado`

**Job automático:**
- Cada día a las 00:05 AM, descuentos con `fechaFin < hoy` cambian a EXPIRADO automáticamente.

**Lógica de vigencia:**
- `estaVigente` = estado ACTIVO + hoy entre fechaInicio y fechaFin

## Flujo de navegación
- **Viene desde:** AdminSidebar ("Descuentos"), Admin Dashboard (sección descuentos por vencer)
- **Puede ir a:** Admin Dashboard
- **Trigger:** Navegación desde sidebar

## Componentes UI
- AdminSidebar
- NavBar admin
- Filtros: tabs (Todos / Activos / Expirados) + búsqueda por nombre
- DataTable: ID, Nombre, Tipo, Valor, Fecha Inicio, Fecha Fin, Estado (badge), Vigente (check/cross), Acciones
- Modal crear/editar descuento
- Widget "Calculadora de descuento" (campo monto + resultado)
- Alert banner sobre el job automático de expiración

## Estados
- [ ] Default — tabla con todos los descuentos
- [ ] Filtrado Activos — solo descuentos ACTIVO
- [ ] Filtrado Expirados — solo descuentos EXPIRADO
- [ ] Modal crear — formulario vacío
- [ ] Modal editar — formulario pre-rellenado
- [ ] Por vencer — warning badge en descuentos que vencen en ≤ 7 días
- [ ] Error nombre duplicado — 409 con mensaje
- [ ] Error fechas — fechaFin < fechaInicio → error de validación

## Notas de UX
- El tipo PORCENTAJE debe mostrar el valor como "15%" en la tabla; el tipo FIJO como "$500".
- El campo `valor` en PORCENTAJE es el número puro (15.00 = 15%); en FIJO es el monto (500.00 = $500).
- Los descuentos con `fechaFin` en los próximos 7 días deben tener un badge de alerta "Vence en X días" en color `--color-warning`.
- El job de expiración corre a las 00:05 AM diariamente. Informar al admin con un banner explicativo.
- Validación frontend: `fechaInicio` no puede ser anterior a hoy para nuevos descuentos.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — Discount Management** for TrailForge back-office. Desktop layout with AdminSidebar.
>
> **Layout:**
> 1. AdminSidebar, "Descuentos" active.
> 2. Page header: "Gestión de Descuentos" Sora H1 + "Nuevo descuento" PrimaryButton right.
> 3. Info banner: "⏰ Los descuentos vencidos se actualizan automáticamente cada día a las 00:05 AM." #F4A261 light background, #1A1A18 text, info icon. Dismissible.
> 4. Filter tabs: "Todos (12)" | "Activos (5)" active | "Expirados (7)".
> 5. **DataTable**:
>    Columns: "ID" | "Nombre" | "Tipo" | "Valor" | "Vigencia" | "Estado" | "¿Vigente?" | "Acciones".
>    Row examples:
>    - "1" | "VERANO20" | TypeBadge "PORCENTAJE" #2D6A4F outline | "20%" | "01/01/26 – 31/03/26" | StatusBadge "ACTIVO" #40916C | ✓ check green | ✎ 🗑
>    - "2" | "DESCUENTO500" | TypeBadge "FIJO" #E76F51 outline | "$500" | "01/05/26 – 20/05/26" | StatusBadge "ACTIVO" #40916C | ⚠ warning icon #F4A261 + "Vence en 2 días" | ✎ 🗑
>    - "3" | "INVIERNO15" | TypeBadge "PORCENTAJE" | "15%" | "01/06/25 – 31/08/25" | StatusBadge "EXPIRADO" #D4CFC8 #8C8880 | ✗ cross gray | ✎ 🗑
> 6. **Create/Edit modal** (480px):
>    "Nuevo Descuento" Sora H2. Form: Nombre input, Tipo radio buttons (⬤ PORCENTAJE | ○ FIJO), Valor number input (with "%" or "$" prefix that changes based on tipo), Fecha inicio date picker, Fecha fin date picker (validates >= inicio), Estado select (ACTIVO/EXPIRADO). "Guardar" + "Cancelar". Validation: "La fecha de fin no puede ser anterior a la de inicio." #C1121F.
> 7. **Discount calculator widget** (below the table, white card): "Calculadora de descuento" Sora H3. Input "Monto base: $" + Select descuento from list + "Calcular" button. Result: "Descuento aplicado: $7.499 | Precio final: $37.498" in #40916C bold.
