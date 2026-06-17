package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrdenResumenResponse {
    Long id;
    Long usuarioId;
    String nombreDestinatario;
    String usuarioNombre;
    String usuarioUsername;
    LocalDateTime fechaCreacion;
    BigDecimal montoFinal;
    EstadoOrden estado;
}
