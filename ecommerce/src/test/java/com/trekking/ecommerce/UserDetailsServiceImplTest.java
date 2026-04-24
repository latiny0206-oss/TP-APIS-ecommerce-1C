package com.trekking.ecommerce;

import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.security.UserDetailsServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_usuarioActivoCliente_retornaUserDetailsHabilitado() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("juan")
                .password("hashed")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("juan");

        assertThat(userDetails.getUsername()).isEqualTo("juan");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    }

    @Test
    void loadUserByUsername_usuarioAdmin_retornaRolAdmin() {
        Usuario usuario = Usuario.builder()
                .id(2L)
                .username("admin")
                .password("hashed")
                .rol(RolUsuario.ADMIN)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_usuarioInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }

    @Test
    void loadUserByUsername_usuarioInactivo_retornaDisabledTrue() {
        Usuario usuario = Usuario.builder()
                .id(3L)
                .username("inactivo")
                .password("hashed")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.INACTIVO)
                .build();
        when(usuarioRepository.findByUsername("inactivo")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("inactivo");

        assertThat(userDetails.isEnabled()).isFalse();
    }
}
