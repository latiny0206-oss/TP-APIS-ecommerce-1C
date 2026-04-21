package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoProducto;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductoResponse {
    Long id;
    Long marcaId;
    String marcaNombre;
    Long categoriaId;
    String categoriaNombre;
    String nombre;
    String descripcion;
    EstadoProducto estado;
    BigDecimal precioBase;
}
