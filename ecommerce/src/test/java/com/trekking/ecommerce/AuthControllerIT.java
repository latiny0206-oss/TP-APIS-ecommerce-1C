package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String USERNAME_EXISTENTE = "auth_it_existing_" + System.nanoTime();
    private static final String EMAIL_EXISTENTE = "existing_" + System.nanoTime() + "@mail.com";
    private static final String PASSWORD_VALIDO = "ValidPass1";

    @BeforeEach
    void prepararUsuarioExistente() {
        usuarioRepository.save(Usuario.builder()
                .username(USERNAME_EXISTENTE)
                .email(EMAIL_EXISTENTE)
                .password(passwordEncoder.encode(PASSWORD_VALIDO))
                .nombre("Existente")
                .apellido("Test")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.ACTIVO)
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void register_exitoso_retorna201ConToken() throws Exception {
        String username = "new_user_" + System.nanoTime();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@mail.com",
                                "password", PASSWORD_VALIDO,
                                "nombre", "Nuevo",
                                "apellido", "Usuario"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void register_siempreComoCLIENTE_noPermiteAdmin() throws Exception {
        String username = "admin_try_" + System.nanoTime();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@mail.com",
                                "password", PASSWORD_VALIDO,
                                "nombre", "Intento",
                                "apellido", "Admin"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void register_usernameDuplicado_retorna409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME_EXISTENTE,
                                "email", "otro_" + System.nanoTime() + "@mail.com",
                                "password", PASSWORD_VALIDO,
                                "nombre", "Otro",
                                "apellido", "Usuario"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    void register_emailDuplicado_retorna409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "other_user_" + System.nanoTime(),
                                "email", EMAIL_EXISTENTE,
                                "password", PASSWORD_VALIDO,
                                "nombre", "Otro",
                                "apellido", "Email"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    void register_passwordSinMayuscula_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "pw_no_upper_" + System.nanoTime(),
                                "email", "noup_" + System.nanoTime() + "@mail.com",
                                "password", "password1",
                                "nombre", "Test",
                                "apellido", "User"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void register_passwordSinNumero_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "pw_no_num_" + System.nanoTime(),
                                "email", "nonum_" + System.nanoTime() + "@mail.com",
                                "password", "Password",
                                "nombre", "Test",
                                "apellido", "User"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void register_passwordMenosDe8Chars_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "pw_short_" + System.nanoTime(),
                                "email", "short_" + System.nanoTime() + "@mail.com",
                                "password", "P1",
                                "nombre", "Test",
                                "apellido", "User"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void register_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "email_inv_" + System.nanoTime(),
                                "email", "no-es-un-email",
                                "password", PASSWORD_VALIDO,
                                "nombre", "Test",
                                "apellido", "User"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void register_camposVacios_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void login_credencialesCorrectas_retorna200ConToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME_EXISTENTE,
                                "password", PASSWORD_VALIDO
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(USERNAME_EXISTENTE));
    }

    @Test
    void login_passwordIncorrecto_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME_EXISTENTE,
                                "password", "WrongPass1"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void login_usernameInexistente_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "no_existe_xyz_99",
                                "password", PASSWORD_VALIDO
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_usuarioInactivo_retorna403() throws Exception {
        String usernameInactivo = "inactivo_auth_" + System.nanoTime();
        usuarioRepository.save(Usuario.builder()
                .username(usernameInactivo)
                .email(usernameInactivo + "@mail.com")
                .password(passwordEncoder.encode(PASSWORD_VALIDO))
                .nombre("Inactivo")
                .apellido("User")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.INACTIVO)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", usernameInactivo,
                                "password", PASSWORD_VALIDO
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message", containsString("inactiva")));
    }

    @Test
    void login_bodyVacio_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
