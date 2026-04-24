package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.MarcaRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.repository.MarcaRepository;
import com.trekking.ecommerce.service.impl.MarcaServiceImpl;
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
class MarcaServiceImplTest {

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private MarcaServiceImpl marcaService;

    @Test
    void findAll_retornaLista() {
        when(marcaRepository.findAll()).thenReturn(List.of(marcaBase()));

        List<Marca> result = marcaService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaMarca() {
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marcaBase()));

        Marca result = marcaService.findById(1L);

        assertThat(result.getNombre()).isEqualTo("Salewa");
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marcaService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_valido_guardaYRetornaMarca() {
        MarcaRequest req = new MarcaRequest();
        req.setNombre("Salewa");
        req.setDescripcion("Marca de montañismo");

        Marca guardada = Marca.builder()
                .id(1L)
                .nombre("Salewa")
                .descripcion("Marca de montañismo")
                .build();

        when(marcaRepository.save(any())).thenReturn(guardada);

        Marca result = marcaService.create(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Salewa");
        verify(marcaRepository).save(any());
    }

    @Test
    void update_existente_actualizaYGuarda() {
        Marca actual = marcaBase();
        MarcaRequest req = new MarcaRequest();
        req.setNombre("Black Diamond");
        req.setDescripcion("Marca de escalada");

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(marcaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Marca result = marcaService.update(1L, req);

        assertThat(result.getNombre()).isEqualTo("Black Diamond");
        assertThat(result.getDescripcion()).isEqualTo("Marca de escalada");
    }

    @Test
    void update_noExistente_lanzaResourceNotFoundException() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marcaService.update(99L, new MarcaRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marcaBase()));

        marcaService.delete(1L);

        verify(marcaRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marcaService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Marca marcaBase() {
        return Marca.builder()
                .id(1L)
                .nombre("Salewa")
                .descripcion("Marca de montañismo")
                .build();
    }
}
