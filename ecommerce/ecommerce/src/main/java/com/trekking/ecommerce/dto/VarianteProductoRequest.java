package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.Estacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VarianteProductoRequest {

    @NotNull
    private Long productoId;

    @NotBlank
    private String color;

    @NotBlank
    private String talla;

    @NotBlank
    private String material;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal peso;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precio;

    @NotNull
    private Estacion estacion;
}
