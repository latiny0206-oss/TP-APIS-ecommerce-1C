package com.trekking.ecommerce.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VarianteBajoStockResponse {
    Long id;
    Long productoId;
    String nombreProducto;
    String color;
    String talla;
    Integer stock;
}
