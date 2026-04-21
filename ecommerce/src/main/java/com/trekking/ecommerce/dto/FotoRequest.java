package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FotoRequest {

    @NotNull
    private Long varianteId;

    @NotBlank
    private String nombre;

    @NotNull
    private Integer orden;
}
