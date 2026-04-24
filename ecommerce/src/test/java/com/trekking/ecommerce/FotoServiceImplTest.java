package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.FotoRepository;
import com.trekking.ecommerce.service.VarianteProductoService;
import com.trekking.ecommerce.service.impl.FotoServiceImpl;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FotoServiceImplTest {

    @Mock private FotoRepository fotoRepository;
    @Mock private VarianteProductoService varianteProductoService;
    @Mock private MultipartFile archivo;

    @InjectMocks
    private FotoServiceImpl fotoService;

    @Test
    void findAll_retornaLista() {
        when(fotoRepository.findAll()).thenReturn(List.of(fotoBase()));

        List<Foto> result = fotoService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_existente_retornaFoto() {
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(fotoBase()));

        Foto result = fotoService.findById(1L);

        assertThat(result.getNombre()).isEqualTo("foto.jpg");
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(fotoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fotoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByVariante_retornaFotosDeLaVariante() {
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();
        when(varianteProductoService.findById(1L)).thenReturn(variante);
        when(fotoRepository.findByVarianteId(1L)).thenReturn(List.of(fotoBase()));

        List<Foto> result = fotoService.findByVariante(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByVariante_varianteInexistente_lanzaResourceNotFoundException() {
        when(varianteProductoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("VarianteProducto", 99L));

        assertThatThrownBy(() -> fotoService.findByVariante(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_valido_guardaFoto() throws Exception {
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();
        Foto guardada = fotoBase();

        when(varianteProductoService.findById(1L)).thenReturn(variante);
        when(archivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getBytes()).thenReturn("contenido".getBytes());
        when(fotoRepository.save(any())).thenReturn(guardada);

        Foto result = fotoService.create(1L, 1, archivo);

        assertThat(result.getNombre()).isEqualTo("foto.jpg");
        verify(fotoRepository).save(any());
    }

    @Test
    void create_varianteInexistente_lanzaResourceNotFoundException() {
        when(archivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(varianteProductoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("VarianteProducto", 99L));

        assertThatThrownBy(() -> fotoService.create(99L, 1, archivo))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_archivoFallaAlLeer_lanzaBusinessRuleException() throws Exception {
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();

        when(varianteProductoService.findById(1L)).thenReturn(variante);
        when(archivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(archivo.getContentType()).thenReturn("image/jpeg");
        when(archivo.getBytes()).thenThrow(new IOException("error de lectura"));

        assertThatThrownBy(() -> fotoService.create(1L, 1, archivo))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se pudo leer el archivo");
    }

    @Test
    void update_valido_actualizaFoto() throws Exception {
        Foto actual = fotoBase();
        VarianteProducto variante = VarianteProducto.builder().id(1L).build();

        when(fotoRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(varianteProductoService.findById(1L)).thenReturn(variante);
        when(archivo.getOriginalFilename()).thenReturn("nuevo.jpg");
        when(archivo.getContentType()).thenReturn("image/png");
        when(archivo.getBytes()).thenReturn("nuevo_contenido".getBytes());
        when(fotoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto result = fotoService.update(1L, 1L, 2, archivo);

        assertThat(result.getNombre()).isEqualTo("nuevo.jpg");
        assertThat(result.getOrden()).isEqualTo(2);
    }

    @Test
    void update_varianteInexistente_lanzaResourceNotFoundException() {
        Foto actual = fotoBase();

        when(archivo.getOriginalFilename()).thenReturn("nuevo.jpg");
        when(archivo.getContentType()).thenReturn("image/png");
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(varianteProductoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("VarianteProducto", 99L));

        assertThatThrownBy(() -> fotoService.update(1L, 99L, 1, archivo))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(fotoBase()));

        fotoService.delete(1L);

        verify(fotoRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(fotoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fotoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Foto fotoBase() {
        return Foto.builder()
                .id(1L)
                .variante(VarianteProducto.builder().id(1L).build())
                .nombre("foto.jpg")
                .tipoContenido("image/jpeg")
                .orden(1)
                .datos("contenido".getBytes())
                .build();
    }
}
