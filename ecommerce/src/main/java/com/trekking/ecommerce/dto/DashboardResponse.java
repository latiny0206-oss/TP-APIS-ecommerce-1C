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

    // KPIs semanales (ventas confirmadas de esta semana vs semana anterior)
    BigDecimal ventasSemanaActual;
    BigDecimal ventasSemanaAnterior;
    Double crecimientoVentasPct;

    // Variantes con bajo stock
    List<VarianteBajoStockResponse> variantesBajoStock;
}
