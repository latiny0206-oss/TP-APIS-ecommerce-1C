package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndpointNegativosBordeIntegrationTest {

    private static final String ADMIN_USERNAME = "admin_test_neg";
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
                        .email("admin.neg@test.com")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .nombre("Admin")
                        .apellido("Neg")
                        .rol(RolUsuario.ADMIN)
                        .estado(EstadoUsuario.ACTIVO)
                        .build()
        ));
    }

    @Test
    void register_conPasswordDebil_devuelve400ValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "cliente_debil_" + System.nanoTime(),
                                "email", "debil_" + System.nanoTime() + "@mail.com",
                                "password", "abc",
                                "nombre", "Cliente",
                                "apellido", "Debil"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void login_conCredencialesInvalidas_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", ADMIN_USERNAME,
                                "password", "PasswordIncorrecta1"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("incorrect")));
    }

    @Test
    void crearDescuentoConPorcentajeInvalido_devuelve400() throws Exception {
        String adminToken = loginAdmin();

        mockMvc.perform(post("/api/descuentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoPorcentajeBody(150.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("100")));
    }

    @Test
    void checkoutCarritoVacio_devuelve400() throws Exception {
        TestUser cliente = registrarCliente("cliente_vacio");
        Long carritoId = crearCarrito(cliente.token());

        mockMvc.perform(post("/api/carritos/{id}/checkout", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("No se puede realizar la compra")));
    }

    @Test
    void confirmarOrdenDosVeces_segundaVezDevuelve400() throws Exception {
        String adminToken = loginAdmin();
        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId, "700.00");
        Long varianteId = crearVariante(adminToken, productoId, 4, "700.00");

        TestUser cliente = registrarCliente("cliente_confirmar");
        Long carritoId = crearCarrito(cliente.token());
        agregarItem(cliente.token(), carritoId, varianteId, 1);

        Long ordenId = checkout(cliente.token(), carritoId);

        mockMvc.perform(post("/api/ordenes/{id}/confirmar", ordenId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        mockMvc.perform(post("/api/ordenes/{id}/confirmar", ordenId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Solo se puede confirmar")));
    }

    @Test
    void descuentoFijoMayorQueSubtotal_totalDelCarritoNoEsNegativo() throws Exception {
        String adminToken = loginAdmin();
        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId, "100.00");
        Long varianteId = crearVariante(adminToken, productoId, 2, "100.00");
        Long descuentoId = crearDescuentoFijo(adminToken, "500.00");

        TestUser cliente = registrarCliente("cliente_borde");
        Long carritoId = crearCarrito(cliente.token());
        actualizarCarrito(cliente.token(), carritoId, descuentoId);
        agregarItem(cliente.token(), carritoId, varianteId, 1);

        MvcResult totalResult = mockMvc.perform(get("/api/carritos/{id}/total", carritoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andReturn();

        BigDecimal total = new BigDecimal(totalResult.getResponse().getContentAsString());
        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void agregarItemConCantidadCero_devuelve400DeValidacion() throws Exception {
        String adminToken = loginAdmin();
        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId, "350.00");
        Long varianteId = crearVariante(adminToken, productoId, 5, "350.00");

        TestUser cliente = registrarCliente("cliente_cantidad");
        Long carritoId = crearCarrito(cliente.token());

        mockMvc.perform(post("/api/carritos/{id}/items", carritoId)
                        .header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idVariante", varianteId,
                                "cantidad", 0
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message", containsString("cantidad")));
    }

    private String loginAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", ADMIN_USERNAME,
                                "password", ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return leerJson(loginResult).get("token").asText();
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

    private Long crearCarrito(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/carritos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private void agregarItem(String token, Long carritoId, Long varianteId, int cantidad) throws Exception {
        mockMvc.perform(post("/api/carritos/{id}/items", carritoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idVariante", varianteId,
                                "cantidad", cantidad
                        ))))
                .andExpect(status().isOk());
    }

    private Long checkout(String token, Long carritoId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/carritos/{id}/checkout", carritoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
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

    private Long crearCategoria(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Categoria Neg " + System.nanoTime(),
                                "descripcion", "Categoria para negativos"
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
                                "nombre", "Marca Neg " + System.nanoTime(),
                                "descripcion", "Marca para negativos"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearProducto(String adminToken, Long categoriaId, Long marcaId, String precioBase) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "marcaId", marcaId,
                                "categoriaId", categoriaId,
                                "nombre", "Producto Neg " + System.nanoTime(),
                                "descripcion", "Producto para negativos",
                                "estado", "ACTIVO",
                                "precioBase", precioBase
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
                                "color", "Azul",
                                "talla", "M" + System.nanoTime(),
                                "material", "Poliester",
                                "peso", "0.80",
                                "stock", stock,
                                "precio", precio,
                                "estacion", "INVIERNO"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Long crearDescuentoFijo(String adminToken, String valor) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/descuentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoFijoBody(valor))))
                .andExpect(status().isCreated())
                .andReturn();

        return leerJson(result).get("id").asLong();
    }

    private Map<String, Object> descuentoPorcentajeBody(double porcentaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Descuento Porc " + System.nanoTime());
        body.put("tipo", "PORCENTAJE");
        body.put("valor", String.valueOf(porcentaje));
        body.put("fechaInicio", LocalDate.now().minusDays(1).toString());
        body.put("fechaFin", LocalDate.now().plusDays(30).toString());
        body.put("estado", "ACTIVO");
        return body;
    }

    private Map<String, Object> descuentoFijoBody(String valor) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Descuento Fijo " + System.nanoTime());
        body.put("tipo", "FIJO");
        body.put("valor", valor);
        body.put("fechaInicio", LocalDate.now().minusDays(1).toString());
        body.put("fechaFin", LocalDate.now().plusDays(30).toString());
        body.put("estado", "ACTIVO");
        return body;
    }

    private JsonNode leerJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record TestUser(String username, String password, String token, Long id) {
    }
}

