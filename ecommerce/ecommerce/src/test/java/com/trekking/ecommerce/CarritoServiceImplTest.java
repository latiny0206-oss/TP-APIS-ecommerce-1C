package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.UsuarioService;
import com.trekking.ecommerce.service.VarianteProductoService;
import com.trekking.ecommerce.service.impl.CarritoServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private VarianteProductoService varianteProductoService;
    @Mock private DescuentoRepository descuentoRepository;
    @Mock private OrdenRepository ordenRepository;
    @Mock private ItemOrdenRepository itemOrdenRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioService usuarioService;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    // ─────────────────────────────────────────────────────────────────────────
    // calcularTotal
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void calcularTotal_retornaLaSumaDeLosItems() {
        Long carritoId = 1L;
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(null)
                .build();

        ItemCarrito item1 = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("100.00"))
                .cantidad(2)
                .build();
        ItemCarrito item2 = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("50.00"))
                .cantidad(3)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item1, item2));

        BigDecimal total = carritoService.calcularTotal(carritoId);

        // 100*2 + 50*3 = 200 + 150 = 350
        assertThat(total).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    void calcularTotal_conDescuentoFijoActivo_restaMonto() {
        Long carritoId = 2L;
        Descuento descuento = Descuento.builder()
                .id(1L)
                .tipo(TipoDescuento.FIJO)
                .valor(new BigDecimal("50.00"))
                .estado(EstadoDescuento.ACTIVO)
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(1))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(descuento)
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("200.00"))
                .cantidad(1)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));

        BigDecimal total = carritoService.calcularTotal(carritoId);

        // 200 - 50 = 150
        assertThat(total).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void calcularTotal_conDescuentoPorcentajeActivo_restaPorcentaje() {
        Long carritoId = 3L;
        Descuento descuento = Descuento.builder()
                .id(2L)
                .tipo(TipoDescuento.PORCENTAJE)
                .valor(new BigDecimal("10"))
                .estado(EstadoDescuento.ACTIVO)
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(1))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(descuento)
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("100.00"))
                .cantidad(2)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));

        BigDecimal total = carritoService.calcularTotal(carritoId);

        // 200 - 10% = 200 - 20 = 180
        assertThat(total).isEqualByComparingTo(new BigDecimal("180.00"));
    }

    @Test
    void calcularTotal_conDescuentoExpirado_noAplicaDescuento() {
        Long carritoId = 4L;
        Descuento descuentoExpirado = Descuento.builder()
                .id(3L)
                .tipo(TipoDescuento.FIJO)
                .valor(new BigDecimal("50.00"))
                .estado(EstadoDescuento.EXPIRADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaFin(LocalDate.now().minusDays(1))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(descuentoExpirado)
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("200.00"))
                .cantidad(1)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));

        BigDecimal total = carritoService.calcularTotal(carritoId);

        // descuento expirado → no aplica → total = 200
        assertThat(total).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // realizarCompra
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void realizarCompra_carritoVacio_lanzaBusinessRuleException() {
        Long carritoId = 10L;
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.VACIO)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of());

        assertThatThrownBy(() -> carritoService.realizarCompra(carritoId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void realizarCompra_stockInsuficiente_lanzaBusinessRuleException() {
        Long carritoId = 11L;
        Long varianteId = 20L;

        Producto producto = Producto.builder()
                .nombre("Campera")
                .estado(EstadoProducto.ACTIVO)
                .build();
        VarianteProducto variante = VarianteProducto.builder()
                .id(varianteId)
                .producto(producto)
                .stock(0)
                .precio(new BigDecimal("500.00"))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .variante(variante)
                .cantidad(2)
                .precioUnitario(variante.getPrecio())
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));
        // descontarStock lanza BusinessRuleException cuando no hay stock
        when(varianteProductoService.descontarStock(varianteId, 2))
                .thenThrow(new BusinessRuleException("Stock insuficiente para variante id " + varianteId));

        assertThatThrownBy(() -> carritoService.realizarCompra(carritoId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void realizarCompra_flujoCompleto_creaOrdenYMarcaCarritoConvertido() {
        Long carritoId = 12L;
        Long varianteId = 30L;

        Producto producto = Producto.builder()
                .nombre("Mochila")
                .estado(EstadoProducto.ACTIVO)
                .build();
        VarianteProducto variante = VarianteProducto.builder()
                .id(varianteId)
                .producto(producto)
                .stock(5)
                .precio(new BigDecimal("300.00"))
                .build();
        Usuario usuario = Usuario.builder().id(1L).username("juan").build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .usuario(usuario)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(null)
                .montoTotal(new BigDecimal("600.00"))
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .variante(variante)
                .cantidad(2)
                .precioUnitario(new BigDecimal("300.00"))
                .build();

        Orden ordenGuardada = Orden.builder()
                .id(1L)
                .usuario(usuario)
                .carrito(carrito)
                .montoFinal(new BigDecimal("600.00"))
                .estado(EstadoOrden.PENDIENTE)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));
        when(ordenRepository.save(any())).thenReturn(ordenGuardada);
        when(carritoRepository.save(any())).thenReturn(carrito);

        Orden resultado = carritoService.realizarCompra(carritoId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoOrden.PENDIENTE);
        assertThat(resultado.getMontoFinal()).isEqualByComparingTo(new BigDecimal("600.00"));
        verify(varianteProductoService).descontarStock(varianteId, 2);
        verify(itemCarritoRepository).deleteAll(List.of(item));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // agregarItem — casos borde
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void agregarItem_productoNoActivo_lanzaBusinessRuleException() {
        Long carritoId = 20L;
        Long varianteId = 40L;

        Producto productoPausado = Producto.builder()
                .nombre("Carpa")
                .estado(EstadoProducto.PAUSADO)
                .build();
        VarianteProducto variante = VarianteProducto.builder()
                .id(varianteId)
                .producto(productoPausado)
                .stock(10)
                .precio(new BigDecimal("1000.00"))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(varianteProductoService.findById(varianteId)).thenReturn(variante);

        assertThatThrownBy(() -> carritoService.agregarItem(carritoId, varianteId, 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void agregarItem_itemExistente_acumulaCantidad() {
        Long carritoId = 21L;
        Long varianteId = 41L;

        Producto producto = Producto.builder()
                .nombre("Botines")
                .estado(EstadoProducto.ACTIVO)
                .build();
        VarianteProducto variante = VarianteProducto.builder()
                .id(varianteId)
                .producto(producto)
                .stock(10)
                .precio(new BigDecimal("200.00"))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(null)
                .build();
        ItemCarrito itemExistente = ItemCarrito.builder()
                .carrito(carrito)
                .variante(variante)
                .cantidad(1)
                .precioUnitario(new BigDecimal("200.00"))
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(varianteProductoService.findById(varianteId)).thenReturn(variante);
        when(itemCarritoRepository.findByCarritoIdAndVarianteId(carritoId, varianteId))
                .thenReturn(java.util.Optional.of(itemExistente));
        when(itemCarritoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(itemExistente));
        when(carritoRepository.save(any())).thenReturn(carrito);

        carritoService.agregarItem(carritoId, varianteId, 3);

        // cantidad original 1 + 3 agregados = 4
        assertThat(itemExistente.getCantidad()).isEqualTo(4);
    }

    @Test
    void actualizarItem_itemDeOtroCarrito_lanzaBusinessRuleException() {
        Long carritoId = 30L;
        Long otroCarritoId = 99L;
        Long itemId = 50L;

        Carrito carritoDelItem = Carrito.builder().id(otroCarritoId).build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carritoDelItem)
                .cantidad(1)
                .build();
        Carrito carritoSolicitado = Carrito.builder().id(carritoId).build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carritoSolicitado));
        when(itemCarritoRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> carritoService.actualizarItem(carritoId, itemId, 2))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no pertenece al carrito");
    }
}
