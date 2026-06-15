package com.trekking.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DashboardResponse {
    long productosActivos;
    long ordenesPendientes;
    long descuentosActivos;
    long clientesRegistrados;
    BigDecimal ventasTotales;
    List<OrdenResumenResponse> ordenesRecientes;
}
