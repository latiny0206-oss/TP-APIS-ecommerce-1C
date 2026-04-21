package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndpointFlujosIntegrationTest {

    private static final String ADMIN_USERNAME = "admin_test";
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
                        .email("admin@test.com")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .nombre("Admin")
                        .apellido("Tests")
                        .rol(RolUsuario.ADMIN)
                        .estado(EstadoUsuario.ACTIVO)
                        .build()
        ));
    }

    @Test
    void registerYLogin_deberiaCrearUsuarioClienteYPermitirAutenticacion() throws Exception {
        String username = "cliente_reg_" + System.nanoTime();
        String password = "ClientePass1";

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@mail.com",
                                "password", password,
                                "nombre", "Juan",
                                "apellido", "Perez"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andReturn();

        String tokenRegistro = leerJson(registerResult).get("token").asText();
        assertThat(tokenRegistro).isNotBlank();
        assertThat(usuarioRepository.findByUsername(username)).isPresent();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void descuentoCreadoPorAdmin_deberiaAplicarseEnTotalDelCarritoYCheckout() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId);
        Long varianteId = crearVariante(adminToken, productoId, 5, "1000.00");
        Long descuentoId = crearDescuentoPorcentaje(adminToken, 10.0);

        TestUser cliente = registrarCliente("cliente_compra");

        Long carritoId = crearCarrito(cliente.token());
        actualizarCarrito(cliente.token(), carritoId, descuentoId);

        mockMvc.perform(post("/api/carritos/{id}/items", carritoId)
                        .header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idVariante", varianteId,
                                "cantidad", 2
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.varianteId").value(varianteId));

        MvcResult totalResult = mockMvc.perform(get("/api/carritos/{id}/total", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andReturn();

        BigDecimal totalConDescuento = new BigDecimal(totalResult.getResponse().getContentAsString());
        assertThat(totalConDescuento).isEqualByComparingTo("1800.00");

        MvcResult checkoutResult = mockMvc.perform(post("/api/carritos/{id}/checkout", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.descuentoId").value(descuentoId))
                .andExpect(jsonPath("$.montoFinal").value(1800.00))
                .andReturn();

        Long ordenId = leerJson(checkoutResult).get("id").asLong();

        mockMvc.perform(post("/api/ordenes/{id}/confirmar", ordenId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        mockMvc.perform(get("/api/carritos/{id}", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONVERTIDO"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(get("/api/variantes/{id}", varianteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void clienteNoDeberiaPoderGestionarDescuentosDeAdmin() throws Exception {
        TestUser cliente = registrarCliente("cliente_sin_admin");

        mockMvc.perform(post("/api/descuentos")
                        .header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoBody(15.0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNoPropietario_noDeberiaAccederAlCarritoDeOtroUsuario() throws Exception {
        TestUser clienteA = registrarCliente("cliente_a");
        TestUser clienteB = registrarCliente("cliente_b");

        Long carritoId = crearCarrito(clienteA.token());

        MvcResult listadoB = mockMvc.perform(get("/api/carritos")
                        .header("Authorization", "Bearer " + clienteB.token()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode carritosB = leerJson(listadoB);
        assertThat(carritosB.isArray()).isTrue();
        assertThat(carritosB.findValues("id").stream().map(JsonNode::asLong).toList())
                .doesNotContain(carritoId);

        mockMvc.perform(get("/api/carritos/{id}", carritoId)
                        .header("Authorization", "Bearer " + clienteB.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelarOrden_deberiaRestaurarStockDeLasVariantes() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId);
        Long varianteId = crearVariante(adminToken, productoId, 5, "500.00");

        TestUser cliente = registrarCliente("cliente_cancelacion");
        Long carritoId = crearCarrito(cliente.token());

        mockMvc.perform(post("/api/carritos/{id}/items", carritoId)
                        .header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idVariante", varianteId,
                                "cantidad", 2
                        ))))
                .andExpect(status().isOk());

        Long ordenId = leerJson(mockMvc.perform(post("/api/carritos/{id}/checkout", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andReturn())
                .get("id").asLong();

        mockMvc.perform(post("/api/ordenes/{id}/cancelar", ordenId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/api/variantes/{id}", varianteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5));
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

        String token = leerJson(registerResult).get("token").asText();
        Long id = usuarioRepository.findByUsername(username).orElseThrow().getId();
        return new TestUser(username, password, token, id);
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

    private Long crearCategoria(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Categoria " + System.nanoTime(),
                                "descripcion", "Categoria creada en test"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearMarca(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/marcas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Marca " + System.nanoTime(),
                                "descripcion", "Marca creada en test"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearProducto(String adminToken, Long categoriaId, Long marcaId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "marcaId", marcaId,
                                "categoriaId", categoriaId,
                                "nombre", "Producto " + System.nanoTime(),
                                "descripcion", "Producto para pruebas",
                                "estado", "ACTIVO",
                                "precioBase", "1000.00"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearVariante(String adminToken, Long productoId, int stock, String precio) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/variantes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productoId", productoId,
                                "color", "Negro",
                                "talla", "L" + System.nanoTime(),
                                "material", "Nylon",
                                "peso", "1.20",
                                "stock", stock,
                                "precio", precio,
                                "estacion", "INVIERNO"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearDescuentoPorcentaje(String adminToken, double porcentaje) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/descuentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoBody(porcentaje))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private void actualizarCarrito(String token, Long carritoId, Long descuentoId) throws Exception {
        mockMvc.perform(put("/api/carritos/{id}", carritoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("descuentoId", descuentoId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuentoId").value(descuentoId));
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

    private Map<String, Object> descuentoBody(double porcentaje) {
        return Map.of(
                "nombre", "Descuento " + System.nanoTime(),
                "tipo", "PORCENTAJE",
                "valor", "10.00",
                "porcentaje", porcentaje,
                "fechaInicio", LocalDate.now().minusDays(1).toString(),
                "fechaFin", LocalDate.now().plusDays(30).toString(),
                "estado", "ACTIVO"
        );
    }

    private JsonNode leerJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record TestUser(String username, String password, String token, Long id) {
    }
}

