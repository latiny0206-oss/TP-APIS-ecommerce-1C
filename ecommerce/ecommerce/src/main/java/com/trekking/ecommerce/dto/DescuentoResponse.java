package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DescuentoResponse {
    Long id;
    String nombre;
    TipoDescuento tipo;
    BigDecimal valor;
    LocalDate fechaInicio;
    LocalDate fechaFin;
    EstadoDescuento estado;
}
