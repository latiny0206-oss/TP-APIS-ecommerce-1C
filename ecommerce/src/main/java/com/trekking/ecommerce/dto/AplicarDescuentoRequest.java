package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AplicarDescuentoRequest {

    @NotBlank
    @Size(max = 50)
    private String codigo;
}
