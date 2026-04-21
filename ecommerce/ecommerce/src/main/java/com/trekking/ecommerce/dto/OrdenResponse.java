package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrdenResponse {
    Long id;
    Long usuarioId;
    Long carritoId;
    Long descuentoId;
    LocalDateTime fechaCreacion;
    BigDecimal montoFinal;
    EstadoOrden estado;
    List<ItemOrdenResponse> items;
}
