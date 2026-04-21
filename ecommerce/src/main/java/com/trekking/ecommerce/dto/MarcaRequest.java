package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarcaRequest {

    @NotBlank
    private String nombre;

    private String descripcion;
}
