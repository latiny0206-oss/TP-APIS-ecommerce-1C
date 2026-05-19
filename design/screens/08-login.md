---
screen: Login
order: 8
device: responsive
status: pending
---

## Descripción
Pantalla de autenticación. El usuario ingresa sus credenciales para obtener un JWT token y acceder a funcionalidades protegidas (carrito, órdenes, perfil).

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `POST /api/auth/login` | Público | Body: `{ username, password }` → Response: `{ token, username, rol }` |

**Request body:**
- `username` (String) — @NotBlank
- `password` (String) — @NotBlank

**Response (AuthResponse):**
- `token` — JWT válido por 24 horas
- `username` — nombre de usuario
- `rol` — ADMIN o CLIENTE

**Posibles errores:**
- 401 — credenciales inválidas (`BadCredentialsException`)
- 403 — cuenta inactiva (`DisabledException`)
- 400 — campos vacíos (validación)

## Flujo de navegación
- **Viene desde:** NavBar (botón Login), Carrito (intento de compra sin auth), Checkout (sesión expirada), cualquier pantalla protegida
- **Puede ir a:** Home (login exitoso como CLIENTE), Admin Dashboard (login exitoso como ADMIN), Registro (link "¿No tenés cuenta?"), Recuperar contraseña (link — no implementado en backend)
- **Trigger:** Click en "Iniciar sesión" con credenciales válidas

## Componentes UI
- Logo TrailForge (centrado)
- Card de formulario centrada
- FormInput "Usuario" (username)
- FormInput "Contraseña" (password, con toggle show/hide)
- Link "¿Olvidaste tu contraseña?" (decorativo, sin endpoint en backend)
- PrimaryButton "Iniciar sesión" (full-width)
- Divider "o"
- Link "¿No tenés cuenta? Registrate"
- Toast/inline error para credenciales inválidas

## Estados
- [ ] Default — formulario vacío
- [ ] Cargando — spinner en botón, inputs disabled
- [ ] Error credenciales — mensaje inline "Usuario o contraseña incorrectos" (#C1121F)
- [ ] Cuenta inactiva — mensaje "Tu cuenta está desactivada. Contactá soporte."
- [ ] Campos vacíos — validación inline en campos requeridos
- [ ] Éxito CLIENTE — redirect a Home o a la página desde donde se redirigió
- [ ] Éxito ADMIN — redirect a Admin Dashboard

## Notas de UX
- El campo `username` mapea exactamente al campo `username` de la entidad Usuario (no al email).
- El JWT dura 24 horas. Al expirar, el usuario debe volver a iniciar sesión; el frontend debe detectar el 401 y redirigir aquí.
- Redirigir al usuario a la pantalla de origen tras login exitoso (patrón "returnUrl").
- El rol en la respuesta (`rol: ADMIN` o `rol: CLIENTE`) determina el redirect post-login.

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Login** page for TrailForge. Device: mobile-first, centered card layout.
>
> **Layout:**
> 1. Full-screen background: #F5F2EE with very subtle mountain silhouette pattern (low opacity #D4CFC8 lines).
> 2. Centered card (white #FFFFFF, 8px radius, medium shadow, max-width 400px, 32px padding):
>    - TrailForge logo + wordmark at top, centered. Logo: stylized mountain peak icon in #2D6A4F.
>    - "Bienvenido de vuelta" Sora H2 #1A1A18, centered, 24px margin-top
>    - "Ingresá a tu cuenta para continuar" Inter body #5C5C56, centered
>    - FormInput "Nombre de usuario": label "Usuario" Inter label 500 #5C5C56, input with #D4CFC8 border 6px radius, person icon left-inside. Focused: #2D6A4F border.
>    - FormInput "Contraseña": label "Contraseña", lock icon left-inside, eye toggle icon right-inside to show/hide.
>    - Error state: input border #C1121F, below the password field: "Usuario o contraseña incorrectos." Inter caption #C1121F with warning icon.
>    - "¿Olvidaste tu contraseña?" link-style text Inter caption #2D6A4F, right-aligned.
>    - PrimaryButton "Iniciar sesión" full-width 48px height #2D6A4F, 6px radius. Loading state: spinner replacing text.
>    - Divider line with "o" text centered in #8C8880.
>    - "¿No tenés cuenta? Registrate →" Inter body centered, "Registrate" in #2D6A4F bold.
> 3. Footer below card: "TrailForge © 2026" Inter caption #8C8880 centered.
>
> Emotional tone: welcoming, secure, trustworthy. Like a trailhead sign that says "You're in the right place."
