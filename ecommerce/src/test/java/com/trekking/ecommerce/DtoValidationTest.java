package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.CategoriaRequest;
import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.dto.ItemCarritoRequest;
import com.trekking.ecommerce.dto.LoginRequest;
import com.trekking.ecommerce.dto.MarcaRequest;
import com.trekking.ecommerce.dto.OrdenRequest;
import com.trekking.ecommerce.dto.ProductoRequest;
import com.trekking.ecommerce.dto.RegisterRequest;
import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.model.enums.Estacion;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerRequest_validaNotBlankEmailSizeYPattern() {
        RegisterRequest dto = new RegisterRequest();
        dto.setUsername(" ");
        dto.setEmail("email-invalido");
        dto.setPassword("abc");
        dto.setNombre("");
        dto.setApellido("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("apellido"));
    }

    @Test
    void usuarioRequest_validaCamposObligatorios() {
        UsuarioRequest dto = new UsuarioRequest();
        dto.setEmail("mail");
        dto.setPassword("sinNumeroA");

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rol"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estado"));
    }

    @Test
    void productoRequest_validaNotNullNotBlankYDecimalMin() {
        ProductoRequest dto = new ProductoRequest();
        dto.setNombre(" ");
        dto.setPrecioBase(new BigDecimal("-1"));

        Set<ConstraintViolation<ProductoRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("marcaId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("categoriaId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estado"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("precioBase"));
    }

    @Test
    void varianteProductoRequest_validaNotBlankNotNullMinYDecimalMin() {
        VarianteProductoRequest dto = new VarianteProductoRequest();
        dto.setColor(" ");
        dto.setTalla(" ");
        dto.setMaterial(" ");
        dto.setPeso(new BigDecimal("-0.01"));
        dto.setStock(-1);
        dto.setPrecio(new BigDecimal("-5"));

        Set<ConstraintViolation<VarianteProductoRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("productoId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("color"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("talla"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("material"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("peso"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("stock"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("precio"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estacion"));
    }

    @Test
    void descuentoRequest_validaNotBlankSizeNotNullYDecimalMin() {
        DescuentoRequest dto = new DescuentoRequest();
        dto.setNombre("x".repeat(101));
        dto.setValor(new BigDecimal("-1"));

        Set<ConstraintViolation<DescuentoRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tipo"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fechaInicio"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fechaFin"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estado"));
    }

    @Test
    void itemCarritoRequest_validaNotNullYMin() {
        ItemCarritoRequest dto = new ItemCarritoRequest();
        dto.setCantidad(0);

        Set<ConstraintViolation<ItemCarritoRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("idVariante"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("cantidad"));
    }

    @Test
    void loginCategoriaYMarca_validanNotBlank() {
        LoginRequest login = new LoginRequest();
        login.setUsername(" ");
        login.setPassword("");
        CategoriaRequest categoria = new CategoriaRequest();
        categoria.setNombre(" ");
        MarcaRequest marca = new MarcaRequest();
        marca.setNombre("");

        assertThat(validator.validate(login)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(validator.validate(categoria)).hasSize(1);
        assertThat(validator.validate(marca)).hasSize(1);
    }

    @Test
    void ordenRequest_validaNotNullYDecimalMin() {
        OrdenRequest dto = new OrdenRequest();
        dto.setMontoFinal(new BigDecimal("-0.1"));

        Set<ConstraintViolation<OrdenRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("usuarioId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fechaCreacion"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("montoFinal"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estado"));
    }

    @Test
    void dtosValidos_noGeneranViolaciones() {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("cliente_ok");
        register.setEmail("cliente@mail.com");
        register.setPassword("ClienteOk1");
        register.setNombre("Nom");
        register.setApellido("Ape");

        UsuarioRequest usuario = new UsuarioRequest();
        usuario.setUsername("admin_ok");
        usuario.setEmail("admin@mail.com");
        usuario.setPassword("AdminPass1");
        usuario.setNombre("Admin");
        usuario.setApellido("Test");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        ProductoRequest producto = new ProductoRequest();
        producto.setMarcaId(1L);
        producto.setCategoriaId(1L);
        producto.setNombre("Producto");
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.setPrecioBase(new BigDecimal("1"));

        VarianteProductoRequest variante = new VarianteProductoRequest();
        variante.setProductoId(1L);
        variante.setColor("Negro");
        variante.setTalla("42");
        variante.setMaterial("Cuero");
        variante.setPeso(new BigDecimal("1"));
        variante.setStock(0);
        variante.setPrecio(new BigDecimal("1"));
        variante.setEstacion(Estacion.OTONO);

        DescuentoRequest descuento = new DescuentoRequest();
        descuento.setNombre("Promo");
        descuento.setTipo(TipoDescuento.FIJO);
        descuento.setValor(new BigDecimal("1"));
        descuento.setFechaInicio(LocalDate.now());
        descuento.setFechaFin(LocalDate.now().plusDays(1));
        descuento.setEstado(EstadoDescuento.ACTIVO);

        OrdenRequest orden = new OrdenRequest();
        orden.setUsuarioId(1L);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setMontoFinal(BigDecimal.ZERO);
        orden.setEstado(EstadoOrden.PENDIENTE);

        assertThat(validator.validate(register)).isEmpty();
        assertThat(validator.validate(usuario)).isEmpty();
        assertThat(validator.validate(producto)).isEmpty();
        assertThat(validator.validate(variante)).isEmpty();
        assertThat(validator.validate(descuento)).isEmpty();
        assertThat(validator.validate(orden)).isEmpty();
    }
}

