package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.AuthResponse;
import com.trekking.ecommerce.dto.ForgotPasswordRequest;
import com.trekking.ecommerce.dto.LoginRequest;
import com.trekking.ecommerce.dto.RegisterRequest;
import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.security.JwtUtil;
import com.trekking.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

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
        return ResponseEntity.ok(Map.of("message",
                "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña."));
    }
}
