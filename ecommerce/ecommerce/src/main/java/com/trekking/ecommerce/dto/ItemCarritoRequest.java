package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCarritoRequest {

    @NotNull(message = "idVariante es requerido")
    private Long idVariante;

    @NotNull(message = "cantidad es requerida")
    @Min(value = 1, message = "cantidad debe ser al menos 1")
    private Integer cantidad;
}
