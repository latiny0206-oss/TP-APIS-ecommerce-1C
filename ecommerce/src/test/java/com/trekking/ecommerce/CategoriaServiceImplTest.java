package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.CategoriaRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Categoria;
import com.trekking.ecommerce.repository.CategoriaRepository;
import com.trekking.ecommerce.service.impl.CategoriaServiceImpl;
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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @Test
    void findAll_retornaLista() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaBase()));

        List<Categoria> result = categoriaService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaBase()));

        Categoria result = categoriaService.findById(1L);

        assertThat(result.getNombre()).isEqualTo("Mochilas");
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_valido_guardaYRetornaCategoria() {
        CategoriaRequest req = new CategoriaRequest();
        req.setNombre("Mochilas");
        req.setDescripcion("Mochilas de trekking");

        Categoria guardada = Categoria.builder()
                .id(1L)
                .nombre("Mochilas")
                .descripcion("Mochilas de trekking")
                .build();

        when(categoriaRepository.save(any())).thenReturn(guardada);

        Categoria result = categoriaService.create(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Mochilas");
        verify(categoriaRepository).save(any());
    }

    @Test
    void update_existente_actualizaYGuarda() {
        Categoria actual = categoriaBase();
        CategoriaRequest req = new CategoriaRequest();
        req.setNombre("Carpas");
        req.setDescripcion("Carpas de camping");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Categoria result = categoriaService.update(1L, req);

        assertThat(result.getNombre()).isEqualTo("Carpas");
        assertThat(result.getDescripcion()).isEqualTo("Carpas de camping");
    }

    @Test
    void update_noExistente_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.update(99L, new CategoriaRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaBase()));

        categoriaService.delete(1L);

        verify(categoriaRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Categoria categoriaBase() {
        return Categoria.builder()
                .id(1L)
                .nombre("Mochilas")
                .descripcion("Mochilas de trekking")
                .build();
    }
}
