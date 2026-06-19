package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.AuthResponse;
import com.trekking.ecommerce.dto.ForgotPasswordRequest;
import com.trekking.ecommerce.dto.LoginRequest;
import com.trekking.ecommerce.dto.RegisterRequest;
import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import com.trekking.ecommerce.dto.ResetPasswordRequest;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.security.JwtUtil;
import com.trekking.ecommerce.service.EmailService;
import com.trekking.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        String rol = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        // userDetails.getUsername() es siempre el username real (no el email que pudo haber ingresado)
        Usuario usuario = usuarioService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(rol)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UsuarioRequest createRequest = new UsuarioRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setEmail(request.getEmail());
        createRequest.setPassword(request.getPassword());
        createRequest.setNombre(request.getNombre());
        createRequest.setApellido(request.getApellido());
        createRequest.setRol(RolUsuario.CLIENTE);
        createRequest.setEstado(EstadoUsuario.ACTIVO);

        UsuarioResponse usuario = usuarioService.create(createRequest);
        
        // Enviar email de bienvenida
        try {
            String welcomeHtml = "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;\">"
                    + "  <div style=\"text-align: center; padding-bottom: 20px; border-bottom: 1px solid #edf2f7;\">"
                    + "    <h1 style=\"color: #2d3748; margin: 0; font-size: 28px;\">¡Bienvenido/a a Cumbre! 🏔️</h1>"
                    + "  </div>"
                    + "  <div style=\"padding: 20px 0; color: #4a5568; line-height: 1.6;\">"
                    + "    <p style=\"font-size: 16px;\">Hola <strong>" + usuario.getNombre() + "</strong>,</p>"
                    + "    <p style=\"font-size: 16px;\">¡Estamos felices de tenerte en nuestra comunidad de montañismo y trekking! Tu cuenta ha sido creada con éxito.</p>"
                    + "    <div style=\"background-color: #f7fafc; padding: 15px; border-radius: 6px; margin: 20px 0; border: 1px solid #edf2f7;\">"
                    + "      <h3 style=\"margin-top: 0; color: #2d3748; font-size: 16px;\">Detalles de tu cuenta:</h3>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Usuario:</strong> " + usuario.getUsername() + "</p>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Email:</strong> " + usuario.getEmail() + "</p>"
                    + "    </div>"
                    + "    <p style=\"font-size: 16px;\">Ya podés iniciar sesión en nuestra tienda, explorar nuestro catálogo y equiparte con lo mejor para tu próxima aventura.</p>"
                    + "    <div style=\"text-align: center; margin: 30px 0;\">"
                    + "      <a href=\"http://localhost:5173/login\" style=\"background-color: #3182ce; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">Ir a la tienda</a>"
                    + "    </div>"
                    + "  </div>"
                    + "  <div style=\"text-align: center; padding-top: 20px; border-top: 1px solid #edf2f7; font-size: 12px; color: #a0aec0;\">"
                    + "    <p style=\"margin: 0;\">© 2026 Cumbre E-commerce. Todos los derechos reservados.</p>"
                    + "  </div>"
                    + "</div>";

            emailService.sendEmail(usuario.getEmail(), "¡Bienvenido a Cumbre! 🏔️", welcomeHtml);
        } catch (Exception e) {
            log.error("Error enviando email de bienvenida: {}", e.getMessage());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            Usuario user = usuarioService.findByEmail(request.getEmail());
            String token = UUID.randomUUID().toString();
            usuarioService.createPasswordResetTokenForUser(user, token);
            
            String resetUrl = "http://localhost:5173/reset-password?token=" + token; // Mover a application.properties en un entorno real
            String htmlContent = "<p>Hola " + user.getNombre() + ",</p>"
                    + "<p>Has solicitado restablecer tu contraseña.</p>"
                    + "<p>Haz clic en el siguiente enlace para crear una nueva contraseña:</p>"
                    + "<a href=\"" + resetUrl + "\">Restablecer contraseña</a>"
                    + "<p>Este enlace expirará en 1 hora.</p>";
                    
            emailService.sendEmail(user.getEmail(), "Restablecer contraseña", htmlContent);
        } catch (Exception e) {
            log.warn("Error enviando email de forgot-password (o usuario no existe): {}", e.getMessage());
        }
        
        return ResponseEntity.ok(Map.of("message",
                "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            usuarioService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
