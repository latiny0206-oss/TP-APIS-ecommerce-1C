package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.impl.ItemCarritoServiceImpl;
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
class ItemCarritoServiceImplTest {

    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private VarianteProductoRepository varianteProductoRepository;

    @InjectMocks
    private ItemCarritoServiceImpl itemCarritoService;

    @Test
    void findAll_retornaLista() {
        when(itemCarritoRepository.findAll()).thenReturn(List.of(itemBase()));

        List<ItemCarrito> result = itemCarritoService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaItem() {
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(itemBase()));

        ItemCarrito result = itemCarritoService.findById(1L);

        assertThat(result.getCantidad()).isEqualTo(2);
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCarritoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_carritoNoExiste_lanzaResourceNotFoundException() {
        ItemCarrito item = itemBase();
        when(carritoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCarritoService.create(item))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Carrito");
    }

    @Test
    void create_varianteNoExiste_lanzaResourceNotFoundException() {
        ItemCarrito item = itemBase();
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(item.getCarrito()));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCarritoService.create(item))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Variante");
    }

    @Test
    void create_valido_guardaYRetornaItem() {
        ItemCarrito item = itemBase();
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(item.getCarrito()));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(item.getVariante()));
        when(itemCarritoRepository.save(any())).thenReturn(item);

        ItemCarrito result = itemCarritoService.create(item);

        assertThat(result.getCantidad()).isEqualTo(2);
        verify(itemCarritoRepository).save(any());
    }

    @Test
    void update_valido_actualizaYRetornaItem() {
        ItemCarrito existente = itemBase();
        existente.setId(1L);
        ItemCarrito nuevo = itemBase();
        nuevo.setCantidad(5);
        nuevo.setPrecioUnitario(new BigDecimal("200.00"));

        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(existente.getCarrito()));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(existente.getVariante()));
        when(itemCarritoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito result = itemCarritoService.update(1L, nuevo);

        assertThat(result.getCantidad()).isEqualTo(5);
        assertThat(result.getPrecioUnitario()).isEqualByComparingTo("200.00");
    }

    @Test
    void update_noExistente_lanzaResourceNotFoundException() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCarritoService.update(99L, itemBase()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(itemBase()));

        itemCarritoService.delete(1L);

        verify(itemCarritoRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemCarritoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ItemCarrito itemBase() {
        Carrito carrito = Carrito.builder().id(1L).build();
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();
        return ItemCarrito.builder()
                .id(1L)
                .carrito(carrito)
                .variante(variante)
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .build();
    }
}
