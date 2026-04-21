package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.ProductoRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Categoria;
import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.CategoriaService;
import com.trekking.ecommerce.service.MarcaService;
import com.trekking.ecommerce.service.impl.ProductoServiceImpl;
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
class ProductoServiceImplTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private VarianteProductoRepository varianteProductoRepository;
    @Mock private MarcaService marcaService;
    @Mock private CategoriaService categoriaService;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void findAll_retornaListaDeProductos() {
        Producto p = productoBase();
        when(productoRepository.findAll()).thenReturn(List.of(p));

        List<Producto> result = productoService.findAll();

        assertThat(result).hasSize(1).contains(p);
    }

    @Test
    void findById_existente_retornaProducto() {
        Producto p = productoBase();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));

        Producto result = productoService.findById(1L);

        assertThat(result.getNombre()).isEqualTo("Mochila");
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_construyeProductoCorrectamente() {
        ProductoRequest req = requestBase();
        Marca marca = Marca.builder().id(1L).nombre("Salewa").build();
        Categoria categoria = Categoria.builder().id(2L).nombre("Mochilas").build();
        Producto guardado = productoBase();
        guardado.setMarca(marca);
        guardado.setCategoria(categoria);

        when(marcaService.findById(1L)).thenReturn(marca);
        when(categoriaService.findById(2L)).thenReturn(categoria);
        when(productoRepository.save(any())).thenReturn(guardado);

        Producto result = productoService.create(req);

        assertThat(result.getNombre()).isEqualTo("Mochila");
        verify(productoRepository).save(any());
    }

    @Test
    void update_actualizaCamposYGuarda() {
        Producto actual = productoBase();
        actual.setId(1L);
        ProductoRequest req = requestBase();
        req.setNombre("Mochila XL");
        Marca marca = Marca.builder().id(1L).nombre("Salewa").build();
        Categoria categoria = Categoria.builder().id(2L).nombre("Mochilas").build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(marcaService.findById(1L)).thenReturn(marca);
        when(categoriaService.findById(2L)).thenReturn(categoria);
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto result = productoService.update(1L, req);

        assertThat(result.getNombre()).isEqualTo("Mochila XL");
    }

    @Test
    void delete_productoExistente_invocaDeleteById() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase()));

        productoService.delete(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    void delete_productoNoExistente_lanzaResourceNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void estaDisponible_productoActivoConStock_retornaTrue() {
        Producto p = productoBase();
        p.setId(1L);
        p.setEstado(EstadoProducto.ACTIVO);
        VarianteProducto v = VarianteProducto.builder().stock(5).build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(varianteProductoRepository.findByProductoId(1L)).thenReturn(List.of(v));

        assertThat(productoService.estaDisponible(1L)).isTrue();
    }

    @Test
    void estaDisponible_productoPausado_retornaFalse() {
        Producto p = productoBase();
        p.setId(1L);
        p.setEstado(EstadoProducto.PAUSADO);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThat(productoService.estaDisponible(1L)).isFalse();
    }

    @Test
    void estaDisponible_productoActivoSinStock_retornaFalse() {
        Producto p = productoBase();
        p.setId(1L);
        p.setEstado(EstadoProducto.ACTIVO);
        VarianteProducto v = VarianteProducto.builder().stock(0).build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(varianteProductoRepository.findByProductoId(1L)).thenReturn(List.of(v));

        assertThat(productoService.estaDisponible(1L)).isFalse();
    }

    @Test
    void findByCategoria_delegaEnRepositorio() {
        when(categoriaService.findById(2L)).thenReturn(Categoria.builder().id(2L).build());
        when(productoRepository.findByCategoriaId(2L)).thenReturn(List.of(productoBase()));

        List<Producto> result = productoService.findByCategoria(2L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByMarca_delegaEnRepositorio() {
        when(marcaService.findById(1L)).thenReturn(Marca.builder().id(1L).build());
        when(productoRepository.findByMarcaId(1L)).thenReturn(List.of(productoBase()));

        List<Producto> result = productoService.findByMarca(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByEstado_delegaEnRepositorio() {
        when(productoRepository.findByEstado(EstadoProducto.ACTIVO)).thenReturn(List.of(productoBase()));

        List<Producto> result = productoService.findByEstado(EstadoProducto.ACTIVO);

        assertThat(result).hasSize(1);
    }

    private Producto productoBase() {
        return Producto.builder()
                .nombre("Mochila")
                .descripcion("Mochila de trekking")
                .estado(EstadoProducto.ACTIVO)
                .precioBase(new BigDecimal("1500.00"))
                .build();
    }

    private ProductoRequest requestBase() {
        ProductoRequest req = new ProductoRequest();
        req.setMarcaId(1L);
        req.setCategoriaId(2L);
        req.setNombre("Mochila");
        req.setDescripcion("Mochila de trekking");
        req.setEstado(EstadoProducto.ACTIVO);
        req.setPrecioBase(new BigDecimal("1500.00"));
        return req;
    }
}
