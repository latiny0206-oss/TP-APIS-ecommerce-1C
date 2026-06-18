package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.Estacion;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.repository.FotoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ProductoService;
import com.trekking.ecommerce.service.impl.VarianteProductoServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VarianteProductoServiceImplTest {

    @Mock private VarianteProductoRepository varianteProductoRepository;
    @Mock private ProductoService productoService;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private ItemOrdenRepository itemOrdenRepository;
    @Mock private FotoRepository fotoRepository;

    @InjectMocks
    private VarianteProductoServiceImpl varianteService;

    @Test
    void findAll_retornaLista() {
        when(varianteProductoRepository.findAll()).thenReturn(List.of(varianteBase(10)));

        List<VarianteProducto> result = varianteService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaVariante() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(5)));

        VarianteProducto result = varianteService.findById(1L);

        assertThat(result.getStock()).isEqualTo(5);
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(varianteProductoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> varianteService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_construyeVarianteCorrectamente() {
        VarianteProductoRequest req = requestBase();
        Producto producto = Producto.builder().id(1L).estado(EstadoProducto.ACTIVO).build();
        VarianteProducto guardada = varianteBase(10);

        when(productoService.findById(1L)).thenReturn(producto);
        when(varianteProductoRepository.save(any())).thenReturn(guardada);

        VarianteProducto result = varianteService.create(req);

        assertThat(result.getStock()).isEqualTo(10);
        verify(varianteProductoRepository).save(any());
    }

    @Test
    void delete_varianteEnCarrito_lanzaBusinessRuleException() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(5)));
        when(itemCarritoRepository.existsByVarianteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> varianteService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("carritos activos");
    }

    @Test
    void delete_varianteEnOrden_lanzaBusinessRuleException() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(5)));
        when(itemCarritoRepository.existsByVarianteId(1L)).thenReturn(false);
        when(itemOrdenRepository.existsByVarianteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> varianteService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("órdenes asociadas");
    }

    @Test
    void delete_varianteSinRelaciones_eliminaCorrectamente() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(5)));
        when(itemCarritoRepository.existsByVarianteId(1L)).thenReturn(false);
        when(itemOrdenRepository.existsByVarianteId(1L)).thenReturn(false);
        when(fotoRepository.findByVarianteId(1L)).thenReturn(List.of());

        varianteService.delete(1L);

        verify(varianteProductoRepository).deleteById(1L);
    }

    @Test
    void descontarStock_stockSuficiente_reduceCorrecto() {
        VarianteProducto v = varianteBase(10);
        v.setId(1L);
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(v));
        when(varianteProductoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VarianteProducto result = varianteService.descontarStock(1L, 3);

        assertThat(result.getStock()).isEqualTo(7);
    }

    @Test
    void descontarStock_stockInsuficiente_lanzaBusinessRuleException() {
        VarianteProducto v = varianteBase(2);
        v.setId(1L);
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> varianteService.descontarStock(1L, 5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void restaurarStock_sumaCantidadCorrectamente() {
        VarianteProducto v = varianteBase(3);
        v.setId(1L);
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(v));
        when(varianteProductoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VarianteProducto result = varianteService.restaurarStock(1L, 2);

        assertThat(result.getStock()).isEqualTo(5);
    }

    @Test
    void tieneStock_cantidadSuficiente_retornaTrue() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(10)));

        assertThat(varianteService.tieneStock(1L, 5)).isTrue();
    }

    @Test
    void tieneStock_cantidadInsuficiente_retornaFalse() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteBase(2)));

        assertThat(varianteService.tieneStock(1L, 5)).isFalse();
    }

    @Test
    void tieneStock_cantidadNula_lanzaBusinessRuleException() {
        assertThatThrownBy(() -> varianteService.tieneStock(1L, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void getPrecio_retornaPrecioDeVariante() {
        VarianteProducto v = varianteBase(5);
        v.setId(1L);
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(v));

        BigDecimal precio = varianteService.getPrecio(1L);

        assertThat(precio).isEqualByComparingTo("500.00");
    }

    private VarianteProducto varianteBase(int stock) {
        return VarianteProducto.builder()
                .id(1L)
                .color("Negro")
                .talla("M")
                .material("Nylon")
                .stock(stock)
                .precio(new BigDecimal("500.00"))
                .estacion(Estacion.INVIERNO)
                .build();
    }

    private VarianteProductoRequest requestBase() {
        VarianteProductoRequest req = new VarianteProductoRequest();
        req.setProductoId(1L);
        req.setColor("Negro");
        req.setTalla("M");
        req.setMaterial("Nylon");
        req.setPeso(new BigDecimal("1.20"));
        req.setStock(10);
        req.setPrecio(new BigDecimal("500.00"));
        req.setEstacion(Estacion.INVIERNO);
        return req;
    }
}
