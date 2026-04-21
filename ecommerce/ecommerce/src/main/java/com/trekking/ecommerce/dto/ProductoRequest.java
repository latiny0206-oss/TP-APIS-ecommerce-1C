package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoProducto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductoRequest {

    @NotNull
    private Long marcaId;

    @NotNull
    private Long categoriaId;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    private EstadoProducto estado;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioBase;
}
