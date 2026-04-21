package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoOrden;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrdenRequest {

    @NotNull
    private Long usuarioId;

    private Long carritoId;

    private Long descuentoId;

    @NotNull
    private LocalDateTime fechaCreacion;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal montoFinal;

    @NotNull
    private EstadoOrden estado;
}
