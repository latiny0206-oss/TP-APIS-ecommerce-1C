---
screen: Registro
order: 9
device: responsive
status: pending
---

## Descripción
Pantalla de registro de nuevos clientes. El usuario completa sus datos personales y credenciales. Al registrarse exitosamente recibe un JWT y accede directamente a la app como CLIENTE.

## Datos del backend

| Endpoint | Método | Datos que consume |
|----------|--------|-------------------|
| `POST /api/auth/register` | Público | Body: RegisterRequest → Response: AuthResponse |

**Request body (RegisterRequest):**
- `username` (String) — @NotBlank, único en el sistema
- `email` (String) — @Email, @NotBlank, único en el sistema
- `password` (String) — @Size(min=8), debe incluir al menos 1 mayúscula y 1 número (regex validation)
- `nombre` (String) — @NotBlank
- `apellido` (String) — @NotBlank

**Rol asignado automáticamente:** CLIENTE (hardcoded en el backend)
**Estado asignado automáticamente:** ACTIVO

**Response (AuthResponse):**
- `token` — JWT (24 horas)
- `username`
- `rol` — siempre CLIENTE para este endpoint

**Posibles errores:**
- 409 Conflict — username o email ya existente (`DataIntegrityViolationException`)
- 400 Bad Request — validación fallida (password débil, email inválido, campos vacíos)

## Flujo de navegación
- **Viene desde:** Login (link "Registrate"), NavBar
- **Puede ir a:** Home (registro exitoso — token guardado, usuario autenticado como CLIENTE)
- **Trigger:** Submit del formulario con todos los campos válidos

## Componentes UI
- Logo TrailForge
- Card de formulario centrada
- FormInput "Nombre"
- FormInput "Apellido"
- FormInput "Nombre de usuario" (username)
- FormInput "Email"
- FormInput "Contraseña" (con requisitos visibles)
- Indicador de fortaleza de contraseña (débil/media/fuerte)
- PrimaryButton "Crear cuenta"
- Link "¿Ya tenés cuenta? Iniciá sesión"

## Estados
- [ ] Default — formulario vacío
- [ ] Cargando — spinner en botón
- [ ] Validación inline — errores por campo al perder foco (blur)
- [ ] Username/email duplicado — error 409 con mensaje "Este usuario/email ya está registrado"
- [ ] Contraseña débil — indicador en rojo, mensaje de requisitos
- [ ] Contraseña suficiente — indicador en verde, todos los requisitos cumplidos
- [ ] Éxito — redirect a Home con JWT guardado, toast "¡Bienvenido/a, [nombre]!"

## Notas de UX
- El campo `username` es el identificador de login (no el email). Debe quedar claro en el placeholder/label que este será el nombre con el que iniciará sesión.
- La contraseña requiere: mínimo 8 caracteres, al menos 1 mayúscula, al menos 1 número. Mostrar estos requisitos como checklist en vivo debajo del input.
- Los errores de unicidad (usuario/email duplicado) llegan como 409 del backend; interpretarlos y mostrar el mensaje específico.
- No hay campo de confirmación de contraseña en el backend, pero es buena práctica de UX incluirlo (validado solo en frontend).

## Prompt para Claude Design

> Design a mobile-first, responsive UI screen for TrailForge, an outdoor trekking e-commerce platform. Visual style: robust, functional, and adventurous. Use the following design tokens strictly: background #F5F2EE (warm cream), surface/cards #FFFFFF, primary color #2D6A4F (forest green), secondary/accent #E76F51 (mountain orange), text primary #1A1A18 (warm near-black), text secondary #5C5C56 (earthy gray), success #40916C, warning #F4A261, error #C1121F, borders/dividers #D4CFC8, disabled/icons #8C8880. Typography: headings in Sora (600/700 weight), body and UI elements in Inter (400/500 weight). Emotional tone should evoke nature trails, durability, and confident exploration — not sterile minimalism. Use subtle earth-tone textures or grain overlays sparingly. All buttons must have clear hover and disabled states. Spacing system: 4px base unit (8, 12, 16, 24, 32, 48px). Border radius: 8px for cards, 6px for buttons and inputs, 20px for badges. Shadows: light (0 2px 8px rgba(0,0,0,0.08)) for cards, medium (0 4px 16px rgba(0,0,0,0.12)) for modals.
>
> Screen: **Registration / Sign Up** page for TrailForge. Device: mobile-first, centered card layout.
>
> **Layout:**
> 1. Full-screen background: #F5F2EE.
> 2. Centered card (white #FFFFFF, 8px radius, shadow, max-width 440px, 32px padding, vertical scroll on mobile):
>    - TrailForge logo + "Creá tu cuenta" Sora H2 #1A1A18, centered.
>    - "Únete a la comunidad TrailForge y equipate para tu próxima aventura." Inter body #5C5C56, centered.
>    - 2-column row: FormInput "Nombre" (placeholder "Juan") + FormInput "Apellido" (placeholder "García").
>    - FormInput "Nombre de usuario": label "Nombre de usuario", placeholder "juan.garcia", note below "Con este nombre iniciarás sesión" Inter caption #8C8880.
>    - FormInput "Email": label "Correo electrónico", placeholder "juan@gmail.com". Valid state: green check icon #40916C right-inside. Error state (duplicate): border #C1121F + "Este email ya está registrado." caption #C1121F.
>    - FormInput "Contraseña": label, lock icon, eye toggle. Below the input: password strength bar (3 segments: red #C1121F / yellow #F4A261 / green #40916C) with label "Débil / Media / Fuerte". Below the bar: small checklist Inter caption 12px: "✓ Mínimo 8 caracteres" (green when met), "✓ Al menos 1 mayúscula", "✓ Al menos 1 número" (each checks off as user types).
>    - FormInput "Confirmar contraseña": match validation, shows "✓ Las contraseñas coinciden" in #40916C or "✗ No coinciden" in #C1121F.
>    - PrimaryButton "Crear mi cuenta" full-width 48px #2D6A4F. Loading state: "Creando cuenta..." with spinner.
>    - "¿Ya tenés cuenta? Iniciá sesión →" Inter body centered, link in #2D6A4F.
>
> Emotional tone: welcoming, exciting, beginning of a journey. First step onto the trail.
