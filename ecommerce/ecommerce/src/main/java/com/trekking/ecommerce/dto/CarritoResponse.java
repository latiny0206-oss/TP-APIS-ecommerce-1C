package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoCarrito;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CarritoResponse {
    Long id;
    Long usuarioId;
    String usuarioUsername;
    Long descuentoId;
    EstadoCarrito estado;
    BigDecimal montoTotal;
    LocalDateTime fechaUltimaModificacion;
    List<ItemCarritoResponse> items;
}
