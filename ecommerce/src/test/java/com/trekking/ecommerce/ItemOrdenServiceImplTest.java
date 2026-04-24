package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.impl.ItemOrdenServiceImpl;
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
class ItemOrdenServiceImplTest {

    @Mock private ItemOrdenRepository itemOrdenRepository;
    @Mock private OrdenRepository ordenRepository;
    @Mock private VarianteProductoRepository varianteProductoRepository;

    @InjectMocks
    private ItemOrdenServiceImpl itemOrdenService;

    @Test
    void findAll_retornaLista() {
        when(itemOrdenRepository.findAll()).thenReturn(List.of(itemBase()));

        List<ItemOrden> result = itemOrdenService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaItem() {
        when(itemOrdenRepository.findById(1L)).thenReturn(Optional.of(itemBase()));

        ItemOrden result = itemOrdenService.findById(1L);

        assertThat(result.getCantidad()).isEqualTo(2);
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(itemOrdenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemOrdenService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ordenNoExiste_lanzaResourceNotFoundException() {
        ItemOrden item = itemBase();
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemOrdenService.create(item))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orden");
    }

    @Test
    void create_varianteNoExiste_lanzaResourceNotFoundException() {
        ItemOrden item = itemBase();
        Orden orden = Orden.builder().id(1L).build();

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemOrdenService.create(item))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Variante");
    }

    @Test
    void create_valido_guardaYRetornaItem() {
        ItemOrden item = itemBase();
        Orden orden = Orden.builder().id(1L).build();
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(variante));
        when(itemOrdenRepository.save(any())).thenReturn(item);

        ItemOrden result = itemOrdenService.create(item);

        assertThat(result.getCantidad()).isEqualTo(2);
        verify(itemOrdenRepository).save(any());
    }

    @Test
    void update_noExistente_lanzaResourceNotFoundException() {
        when(itemOrdenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemOrdenService.update(99L, itemBase()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_valido_actualizaYRetornaItem() {
        ItemOrden existente = itemBase();
        existente.setId(1L);
        ItemOrden nuevo = itemBase();
        nuevo.setCantidad(5);
        nuevo.setPrecioAlMomento(new BigDecimal("200.00"));
        Orden orden = Orden.builder().id(1L).build();
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();

        when(itemOrdenRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(variante));
        when(itemOrdenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemOrden result = itemOrdenService.update(1L, nuevo);

        assertThat(result.getCantidad()).isEqualTo(5);
        assertThat(result.getPrecioAlMomento()).isEqualByComparingTo("200.00");
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(itemOrdenRepository.findById(1L)).thenReturn(Optional.of(itemBase()));

        itemOrdenService.delete(1L);

        verify(itemOrdenRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(itemOrdenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemOrdenService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ItemOrden itemBase() {
        Orden orden = Orden.builder().id(1L).build();
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();
        return ItemOrden.builder()
                .id(1L)
                .orden(orden)
                .variante(variante)
                .cantidad(2)
                .precioAlMomento(new BigDecimal("100.00"))
                .build();
    }
}
