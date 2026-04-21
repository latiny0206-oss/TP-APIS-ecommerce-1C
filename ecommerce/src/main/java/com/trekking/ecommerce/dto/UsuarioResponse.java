package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioResponse {
    Long id;
    String username;
    String email;
    String nombre;
    String apellido;
    RolUsuario rol;
    EstadoUsuario estado;
}

