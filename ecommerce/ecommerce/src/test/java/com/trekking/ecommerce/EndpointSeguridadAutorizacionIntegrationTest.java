package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndpointSeguridadAutorizacionIntegrationTest {

    private static final String ADMIN_USERNAME = "admin_test_authz";
    private static final String ADMIN_PASSWORD = "AdminPass1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void asegurarAdmin() {
        usuarioRepository.findByUsername(ADMIN_USERNAME).orElseGet(() -> usuarioRepository.save(
                Usuario.builder()
                        .username(ADMIN_USERNAME)
                        .email("admin.authz@test.com")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .nombre("Admin")
                        .apellido("Authz")
                        .rol(RolUsuario.ADMIN)
                        .estado(EstadoUsuario.ACTIVO)
                        .build()
        ));
    }

    @Test
    void endpointPublico_catalogoSinToken_deberiaResponder200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegido_carritosSinToken_deberiaRechazarAcceso() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/carritos"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void clienteNoPuedeAccederEndpointAdmin_usuarios() throws Exception {
        TestUser cliente = registrarCliente("cliente_no_admin");

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSiPuedeAccederEndpointAdmin_usuarios() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void clientePuedeAccederASuPropioCarrito_porId() throws Exception {
        TestUser clienteA = registrarCliente("cliente_a_authz");

        Long carritoA = crearCarrito(clienteA.token());

        mockMvc.perform(get("/api/carritos/{id}", carritoA)
                        .header("Authorization", "Bearer " + clienteA.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carritoA));
    }

    @Test
    void clienteNoPuedeAccederCarritoDeOtroUsuario_deberiaRetornar403() throws Exception {
        TestUser clienteA = registrarCliente("cliente_owner_a");
        TestUser clienteB = registrarCliente("cliente_owner_b");

        Long carritoA = crearCarrito(clienteA.token());

        mockMvc.perform(get("/api/carritos/{id}", carritoA)
                        .header("Authorization", "Bearer " + clienteB.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void adminPuedeAccederCarritoAjeno_deberiaRetornar200() throws Exception {
        TestUser cliente = registrarCliente("cliente_owner_admin");
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        Long carritoCliente = crearCarrito(cliente.token());

        mockMvc.perform(get("/api/carritos/{id}", carritoCliente)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carritoCliente));
    }

    @Test
    void descuentosActivos_requiereAutenticacionPeroNoRolAdmin() throws Exception {
        TestUser cliente = registrarCliente("cliente_descuentos");

        mockMvc.perform(get("/api/descuentos/activos")
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk());
    }

    private TestUser registrarCliente(String prefijo) throws Exception {
        String username = prefijo + "_" + System.nanoTime();
        String password = "ClientePass1";

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@mail.com",
                                "password", password,
                                "nombre", "Nombre",
                                "apellido", "Apellido"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return new TestUser(
                username,
                password,
                leerJson(registerResult).get("token").asText(),
                usuarioRepository.findByUsername(username).orElseThrow().getId()
        );
    }

    private String login(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return leerJson(loginResult).get("token").asText();
    }

    private Long crearCarrito(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/carritos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private JsonNode leerJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }


    private record TestUser(String username, String password, String token, Long id) {
    }
}

