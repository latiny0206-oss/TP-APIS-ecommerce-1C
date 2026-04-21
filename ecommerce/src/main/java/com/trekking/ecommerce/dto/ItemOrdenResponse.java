package com.trekking.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ItemOrdenResponse {
    Long id;
    Long varianteId;
    String varianteColor;
    String varianteTalla;
    String productoNombre;
    Integer cantidad;
    BigDecimal precioAlMomento;
}
