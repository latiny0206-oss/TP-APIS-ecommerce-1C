---
screen: Admin — Gestión de Órdenes
order: 17
device: responsive
status: pending
---

## Descripción
Panel de administración para visualizar y gestionar todas las órdenes de todos los usuarios. El admin puede confirmar, cancelar y filtrar órdenes por estado.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/ordenes` | ADMIN | Todas las órdenes del sistema |
| `GET /api/ordenes/{id}` | ADMIN | Detalle de una orden |
| `GET /api/ordenes/{id}/items` | ADMIN | Items de una orden |
| `POST /api/ordenes/{id}/confirmar` | ADMIN | Confirmar orden (PENDIENTE → CONFIRMADA) |
| `POST /api/ordenes/{id}/cancelar` | ADMIN | Cancelar orden (→ CANCELADA, restaura stock) |
| `GET /api/ordenes/usuario/{idUsuario}` | ADMIN | Órdenes de un usuario específico |

**Datos por OrdenResponse:**
- `id`, `usuarioId`, `fechaCreacion`, `montoFinal`, `estado`, `items`

**Transiciones de estado:**
- PENDIENTE → CONFIRMADA (acción: confirmar)
- PENDIENTE → CANCELADA (acción: cancelar)
- CONFIRMADA → CANCELADA (acción: cancelar)
- ENTREGADA — estado final, sin transiciones
- CANCELADA — estado final, sin transiciones

## Flujo de navegación
- **Viene desde:** AdminSidebar ("Órdenes"), Admin Dashboard (tabla de órdenes recientes)
- **Puede ir a:** Detalle de orden (panel lateral o nueva vista), Admin Dashboard
- **Trigger:** Navegación desde sidebar

## Componentes UI
- AdminSidebar
- NavBar admin
- Filtros: tabs de estado (Todas / Pendientes / Confirmadas / Entregadas / Canceladas) + filtro por usuario
- DataTable: ID, Usuario, Fecha, Items (count), Monto Final, Estado (StatusBadge), Acciones
- Panel de detalle de orden (slide-in lateral en desktop o página separada en mobile)
- Botones de acción por fila: "Confirmar" (si PENDIENTE), "Cancelar" (si PENDIENTE/CONFIRMADA)
- ConfirmDialog para cancelar

## Estados
- [ ] Default — tabla con todas las órdenes, ordenadas por fecha desc
- [ ] Filtrado por estado — tab activo, tabla filtrada
- [ ] Detalle abierto — panel lateral con items de la orden
- [ ] Confirmando — spinner en fila
- [ ] Cancelando — ConfirmDialog → spinner → estado actualizado
- [ ] Sin órdenes — EmptyState

## Notas de UX
- Al cancelar una orden, el backend restaura el stock de todas las variantes involucradas (lógica en el servicio).
- El estado ENTREGADA no tiene un endpoint en el backend actual; sería una futura extensión.
- Mostrar el nombre de usuario (campo `usuarioId` + lookup a `/api/usuarios/{id}`) en la tabla para identificar al cliente.
- La cancelación masiva no está soportada por el backend; hacerlo de a una orden.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin — Order Management** for TrailForge back-office. Desktop layout with AdminSidebar.
>
> **Layout:**
> 1. AdminSidebar, "Órdenes" active.
> 2. Page header: "Gestión de Órdenes" Sora H1 + "47 órdenes en total" subtitle.
> 3. Filter/tab bar: "Todas (47)" | "Pendientes (12)" active with #2D6A4F underline | "Confirmadas (28)" | "Entregadas (5)" | "Canceladas (2)".
> 4. **DataTable** (white surface):
>    Columns: "#" 60px | "Cliente" 150px | "Fecha" 140px | "Ítems" 80px | "Monto" 110px | "Estado" 130px | "Acciones" 150px.
>    Row examples:
>    - "#1042" | "juan.garcia" | "18 may 2026 14:32" | "3 ítems" | "$67.497" | StatusBadge "PENDIENTE" #F4A261 | "Confirmar" green button small + "Cancelar" red button small
>    - "#1041" | "maria.lopez" | "17 may 2026 09:15" | "1 ítem" | "$24.999" | StatusBadge "CONFIRMADA" #40916C | "Cancelar" red small
>    - "#1040" | "pedro.smith" | "15 may 2026 16:00" | "5 ítems" | "$112.000" | StatusBadge "ENTREGADA" #2D6A4F | "—" (no actions)
>    Row hover: light #F5F2EE bg, cursor pointer.
> 5. **Order detail slide-in panel** (right 420px, white surface, shadow, opens when row clicked):
>    - Header: "Orden #1042" Sora H2 + StatusBadge + × close
>    - Client: "juan.garcia" with user icon
>    - Date: formatted date
>    - Items list: each OrderItem thumbnail 48px + name + qty + price
>    - Totals: subtotal + descuento + total
>    - Action buttons: "Confirmar orden" PrimaryButton + "Cancelar orden" DangerButton outline
> 6. **Cancel ConfirmDialog**: "¿Cancelar la Orden #1042? El stock de 3 variantes será restaurado automáticamente." DangerButton + SecondaryButton.
