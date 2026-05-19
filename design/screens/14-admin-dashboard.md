---
screen: Admin — Dashboard
order: 14
device: responsive
status: pending
---

## Descripción
Panel central del administrador. Vista general del estado del negocio: órdenes recientes, productos con stock bajo, descuentos activos y métricas rápidas.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `GET /api/ordenes` | ADMIN | Todas las órdenes (para contar por estado) |
| `GET /api/productos/estado/ACTIVO` | Público | Productos activos (para contar) |
| `GET /api/variantes` | Público | Variantes (para detectar stock bajo) |
| `GET /api/descuentos/activos` | Autenticado | Descuentos vigentes actualmente |
| `GET /api/usuarios` | ADMIN | Lista de usuarios (para contar clientes) |

**Métricas calculadas (client-side sobre los datos del backend):**
- Total de órdenes / por estado (PENDIENTE, CONFIRMADA, ENTREGADA, CANCELADA)
- Productos con stock crítico (variantes con stock < 5)
- Descuentos activos que vencen en los próximos 7 días
- Total de usuarios registrados

## Flujo de navegación
- **Viene desde:** Login (admin exitoso), AdminSidebar (ítem "Dashboard")
- **Puede ir a:** Admin Productos, Admin Órdenes, Admin Usuarios, Admin Descuentos, Admin Categorías/Marcas
- **Trigger:** Login exitoso con rol ADMIN

## Componentes UI
- AdminSidebar (menú lateral: Dashboard, Productos, Variantes, Categorías, Marcas, Órdenes, Usuarios, Descuentos)
- NavBar admin (logo + avatar admin + "Cerrar sesión")
- Grid de KPI cards (4 métricas principales)
- Lista de órdenes recientes (últimas 5, con estado)
- Lista de variantes con stock crítico (stock < 5)
- Lista de descuentos próximos a vencer

## Estados
- [ ] Default — dashboard cargado con datos
- [ ] Loading — skeleton en KPI cards y listas
- [ ] Sin datos — sección vacía si no hay órdenes/descuentos

## Notas de UX
- El AdminSidebar debe ser colapsable en mobile (hamburger menu).
- Las alertas de stock crítico son accionables: click en el ítem lleva a Admin Variantes con ese producto pre-filtrado.
- Los colores de estado de orden en la tabla siguen el mismo sistema de StatusBadge del cliente.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Admin Dashboard** for TrailForge back-office. Device: desktop-first (sidebar layout), with mobile responsive (sidebar collapses to top nav).
>
> **Layout:**
> 1. **AdminSidebar** — left fixed 240px, #1A1A18 background. Top: TrailForge logo in white. Nav items with icons: "📊 Dashboard" (active, #2D6A4F bg, white text), "📦 Productos", "🎨 Variantes", "🏷 Categorías", "🏭 Marcas", "📋 Órdenes", "👥 Usuarios", "🎁 Descuentos". Each item: 48px height, Inter label 500, #8C8880 inactive color, white hover. Bottom: avatar + "Admin" label + logout icon.
> 2. **Main content area** (background #F5F2EE, padding 24px):
>    - Top bar: "Dashboard" Sora H1 #1A1A18 + current date subtitle Inter body #5C5C56.
>    - **KPI grid** (2×2 on mobile, 4×1 on desktop): each KPI card white surface 8px radius shadow: (a) "Órdenes totales" icon 📋, number "47" Sora H1 #2D6A4F, subtitle "12 pendientes" #F4A261; (b) "Productos activos" icon 📦, "38" #2D6A4F, "3 sin stock" #C1121F; (c) "Clientes registrados" icon 👥, "124" #2D6A4F; (d) "Descuentos activos" icon 🎁, "5" #2D6A4F, "2 vencen esta semana" #F4A261.
>    - **Recent orders section**: "Órdenes recientes" Sora H2 + "Ver todas →" link #2D6A4F. Table with columns: #, Cliente, Fecha, Monto, Estado (StatusBadge), Acciones (eye icon). Show 5 rows. Alternating row bg: white / #F5F2EE.
>    - **Critical stock section**: "Stock crítico ⚠" Sora H2 #C1121F. List of cards: product variant image + name + "Color: Rojo | Talla 42" + StockBadge "2 unidades" #F4A261. "Editar variante →" link.
>    - **Expiring discounts section**: "Descuentos próximos a vencer" Sora H2 #F4A261. List: discount name, tipo (PORCENTAJE/FIJO), vence en "3 días" warning text.
>
> Emotional tone: operational command center — clear, data-dense but not cluttered.
