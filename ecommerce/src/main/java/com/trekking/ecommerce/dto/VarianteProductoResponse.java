package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.Estacion;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VarianteProductoResponse {
    Long id;
    Long productoId;
    String productoNombre;
    String color;
    String talla;
    String material;
    BigDecimal peso;
    Integer stock;
    BigDecimal precio;
    Estacion estacion;
}
