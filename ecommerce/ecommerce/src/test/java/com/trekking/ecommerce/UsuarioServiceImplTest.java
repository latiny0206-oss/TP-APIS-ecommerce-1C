package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.impl.UsuarioServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void findAll_retornaListaDeResponses() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioBase()));

        List<UsuarioResponse> result = usuarioService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("juan");
    }

    @Test
    void findById_existente_retornaResponse() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase()));

        UsuarioResponse response = usuarioService.findById(1L);

        assertThat(response.getUsername()).isEqualTo("juan");
        assertThat(response.getRol()).isEqualTo(RolUsuario.CLIENTE);
    }

    @Test
    void findById_noExistente_lanzaResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_codificaPasswordYGuarda() {
        UsuarioRequest req = requestBase();
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse response = usuarioService.create(req);

        assertThat(response.getUsername()).isEqualTo("juan");
        verify(passwordEncoder).encode("Password1");
    }

    @Test
    void update_sinPassword_noRecodifica() {
        Usuario existente = usuarioBase();
        existente.setPassword("hashed_original");
        UsuarioRequest req = requestBase();
        req.setPassword(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.update(1L, req);

        assertThat(existente.getPassword()).isEqualTo("hashed_original");
    }

    @Test
    void update_conPassword_recodificaPassword() {
        Usuario existente = usuarioBase();
        existente.setPassword("hashed_original");
        UsuarioRequest req = requestBase();
        req.setPassword("NuevoPass1");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("NuevoPass1")).thenReturn("hashed_nuevo");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.update(1L, req);

        assertThat(existente.getPassword()).isEqualTo("hashed_nuevo");
        verify(passwordEncoder).encode("NuevoPass1");
    }

    @Test
    void delete_existente_invocaDeleteById() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase()));

        usuarioService.delete(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUsername_existente_retornaEntidad() {
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuarioBase()));

        Usuario result = usuarioService.findByUsername("juan");

        assertThat(result.getUsername()).isEqualTo("juan");
    }

    @Test
    void findByUsername_noExistente_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByUsername("fantasma"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("fantasma");
    }

    @Test
    void findEntityById_existente_retornaEntidad() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase()));

        Usuario result = usuarioService.findEntityById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    private Usuario usuarioBase() {
        return Usuario.builder()
                .id(1L)
                .username("juan")
                .email("juan@mail.com")
                .password("hashed")
                .nombre("Juan")
                .apellido("Perez")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.ACTIVO)
                .build();
    }

    private UsuarioRequest requestBase() {
        UsuarioRequest req = new UsuarioRequest();
        req.setUsername("juan");
        req.setEmail("juan@mail.com");
        req.setPassword("Password1");
        req.setNombre("Juan");
        req.setApellido("Perez");
        req.setRol(RolUsuario.CLIENTE);
        req.setEstado(EstadoUsuario.ACTIVO);
        return req;
    }
}
