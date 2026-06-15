package com.trekking.ecommerce.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ContactoResponse {
    Long id;
    String nombre;
    String email;
    String asunto;
    String mensaje;
    LocalDateTime fechaEnvio;
    boolean leido;
}
