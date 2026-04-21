package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.VarianteProductoService;
import com.trekking.ecommerce.service.impl.OrdenServiceImpl;
import java.math.BigDecimal;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceImplTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private ItemOrdenRepository itemOrdenRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private DescuentoRepository descuentoRepository;
    @Mock private VarianteProductoService varianteProductoService;

    @InjectMocks
    private OrdenServiceImpl ordenService;

    @Test
    void findAll_retornaLista() {
        when(ordenRepository.findAll()).thenReturn(List.of(ordenBase(EstadoOrden.PENDIENTE)));

        List<Orden> result = ordenService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaOrden() {
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        Orden result = ordenService.findById(1L);

        assertThat(result.getEstado()).isEqualTo(EstadoOrden.PENDIENTE);
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmar_ordenPendiente_cambia_aConfirmada() {
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Orden result = ordenService.confirmar(1L);

        assertThat(result.getEstado()).isEqualTo(EstadoOrden.CONFIRMADA);
    }

    @Test
    void confirmar_ordenNoEsPendiente_lanzaBusinessRuleException() {
        Orden orden = ordenBase(EstadoOrden.CONFIRMADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.confirmar(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    void cancelar_ordenPendiente_restauraStockYCancela() {
        Long varianteId = 10L;
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        orden.setId(1L);
        VarianteProducto variante = VarianteProducto.builder().id(varianteId).build();
        ItemOrden item = ItemOrden.builder().variante(variante).cantidad(2).build();

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(itemOrdenRepository.findByOrdenId(1L)).thenReturn(List.of(item));
        when(ordenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Orden result = ordenService.cancelar(1L);

        assertThat(result.getEstado()).isEqualTo(EstadoOrden.CANCELADA);
        verify(varianteProductoService).restaurarStock(varianteId, 2);
    }

    @Test
    void cancelar_ordenEntregada_lanzaBusinessRuleException() {
        Orden orden = ordenBase(EstadoOrden.ENTREGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.cancelar(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ENTREGADA");
    }

    @Test
    void cancelar_ordenYaCancelada_lanzaBusinessRuleException() {
        Orden orden = ordenBase(EstadoOrden.CANCELADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenService.cancelar(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CANCELADA");
    }

    @Test
    void delete_ordenPendiente_restauraStockYElimina() {
        Long varianteId = 20L;
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        orden.setId(2L);
        VarianteProducto variante = VarianteProducto.builder().id(varianteId).build();
        ItemOrden item = ItemOrden.builder().variante(variante).cantidad(3).build();

        when(ordenRepository.findById(2L)).thenReturn(Optional.of(orden));
        when(itemOrdenRepository.findByOrdenId(2L)).thenReturn(List.of(item));

        ordenService.delete(2L);

        verify(varianteProductoService).restaurarStock(varianteId, 3);
        verify(ordenRepository).deleteById(2L);
    }

    @Test
    void delete_ordenCancelada_noRestaurarStock() {
        Orden orden = ordenBase(EstadoOrden.CANCELADA);
        orden.setId(3L);
        when(ordenRepository.findById(3L)).thenReturn(Optional.of(orden));

        ordenService.delete(3L);

        verify(varianteProductoService, never()).restaurarStock(any(), any());
        verify(ordenRepository).deleteById(3L);
    }

    @Test
    void getMontoFinal_retornaMontoCorrectamente() {
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        orden.setMontoFinal(new BigDecimal("999.00"));
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        BigDecimal monto = ordenService.getMontoFinal(1L);

        assertThat(monto).isEqualByComparingTo("999.00");
    }

    @Test
    void findByUsuario_retornaOrdenesDelUsuario() {
        when(ordenRepository.findByUsuarioId(1L)).thenReturn(List.of(ordenBase(EstadoOrden.PENDIENTE)));

        List<Orden> result = ordenService.findByUsuario(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerItems_retornaItemsDeOrden() {
        Orden orden = ordenBase(EstadoOrden.PENDIENTE);
        orden.setId(1L);
        ItemOrden item = ItemOrden.builder().cantidad(1).build();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(itemOrdenRepository.findByOrdenId(1L)).thenReturn(List.of(item));

        List<ItemOrden> items = ordenService.obtenerItems(1L);

        assertThat(items).hasSize(1);
    }

    private Orden ordenBase(EstadoOrden estado) {
        return Orden.builder()
                .id(1L)
                .usuario(Usuario.builder().id(1L).build())
                .montoFinal(new BigDecimal("500.00"))
                .estado(estado)
                .build();
    }
}
