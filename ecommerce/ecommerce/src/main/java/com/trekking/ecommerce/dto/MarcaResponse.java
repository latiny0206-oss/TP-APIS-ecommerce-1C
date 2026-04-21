package com.trekking.ecommerce.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MarcaResponse {
    Long id;
    String nombre;
    String descripcion;
}
