package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DescuentoRequest {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotNull
    private TipoDescuento tipo;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal valor;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    private EstadoDescuento estado;

}
