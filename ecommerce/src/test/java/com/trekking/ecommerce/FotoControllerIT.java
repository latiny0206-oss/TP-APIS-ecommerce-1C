package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FotoControllerIT {

    private static final String ADMIN_USERNAME = "admin_foto_it";
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
                        .email("admin.foto.it@test.com")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .nombre("Admin")
                        .apellido("Foto")
                        .rol(RolUsuario.ADMIN)
                        .estado(EstadoUsuario.ACTIVO)
                        .build()
        ));
    }

    @Test
    void getPublicos_sinToken_responden200() throws Exception {
        mockMvc.perform(get("/api/fotos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/fotos/variante/{varianteId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void postFoto_clienteNoPuedeCrear_adminSiPuede() throws Exception {
        String clienteToken = registrarClienteYObtenerToken("foto_cliente");
        String adminToken = loginAdmin();
        Long varianteId = crearVariante(adminToken);

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen-test".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/fotos")
                        .file(archivo)
                        .param("varianteId", String.valueOf(varianteId))
                        .param("orden", "1")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/fotos")
                        .file(archivo)
                        .param("varianteId", String.valueOf(varianteId))
                        .param("orden", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.varianteId").value(varianteId))
                .andExpect(jsonPath("$.nombre").value("foto.jpg"))
                .andExpect(jsonPath("$.datos").isString());
    }

    @Test
    void putYDeleteFoto_adminPuedeGestionar() throws Exception {
        String adminToken = loginAdmin();
        Long varianteId = crearVariante(adminToken);
        Long fotoId = crearFoto(adminToken, varianteId);

        MockMultipartFile nuevoArchivo = new MockMultipartFile(
                "archivo",
                "foto-updated.png",
                MediaType.IMAGE_PNG_VALUE,
                "contenido-nuevo".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/fotos/{id}", fotoId)
                        .file(nuevoArchivo)
                        .param("varianteId", String.valueOf(varianteId))
                        .param("orden", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fotoId))
                .andExpect(jsonPath("$.nombre").value("foto-updated.png"))
                .andExpect(jsonPath("$.orden").value(2));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/fotos/{id}", fotoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void createConVarianteInexistente_retorna404() throws Exception {
        String adminToken = loginAdmin();
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "x".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/fotos")
                        .file(archivo)
                        .param("varianteId", "999999")
                        .param("orden", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Long crearFoto(String adminToken, Long varianteId) throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "foto-base.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "base".getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/api/fotos")
                        .file(archivo)
                        .param("varianteId", String.valueOf(varianteId))
                        .param("orden", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String loginAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", ADMIN_USERNAME,
                                "password", ADMIN_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String registrarClienteYObtenerToken(String base) throws Exception {
        String username = base + "_" + System.nanoTime();
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@mail.com",
                                "password", "ClientePass1",
                                "nombre", "Nombre",
                                "apellido", "Apellido"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private Long crearVariante(String adminToken) throws Exception {
        Long categoriaId = crearCategoria(adminToken);
        Long marcaId = crearMarca(adminToken);
        Long productoId = crearProducto(adminToken, categoriaId, marcaId);

        MvcResult result = mockMvc.perform(post("/api/variantes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productoId", productoId,
                                "color", "Negro",
                                "talla", "42-" + System.nanoTime(),
                                "material", "Cuero",
                                "peso", "1.20",
                                "stock", 10,
                                "precio", "1000.00",
                                "estacion", "OTONO"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crearCategoria(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Cat Foto " + System.nanoTime(),
                                "descripcion", "Cat test"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crearMarca(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/marcas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Marca Foto " + System.nanoTime(),
                                "descripcion", "Marca test"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crearProducto(String adminToken, Long categoriaId, Long marcaId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "marcaId", marcaId,
                                "categoriaId", categoriaId,
                                "nombre", "Prod Foto " + System.nanoTime(),
                                "descripcion", "Producto test",
                                "estado", "ACTIVO",
                                "precioBase", "1000.00"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}

