package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.DashboardResponse;
import com.trekking.ecommerce.dto.OrdenResumenResponse;
import com.trekking.ecommerce.dto.VarianteBajoStockResponse;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductoRepository productoRepository;
    private final OrdenRepository ordenRepository;
    private final DescuentoRepository descuentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VarianteProductoRepository varianteRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        // ── Órdenes recientes ─────────────────────────────────────────────
        List<OrdenResumenResponse> ordenesRecientes = ordenRepository
                .findTop5ByOrderByFechaCreacionDesc()
                .stream()
                .map(o -> OrdenResumenResponse.builder()
                        .id(o.getId())
                        .usuarioId(o.getUsuario().getId())
                        .nombreDestinatario(o.getNombreDestinatario())
                        .usuarioNombre(o.getUsuario().getNombre() + " " + o.getUsuario().getApellido())
                        .usuarioUsername(o.getUsuario().getUsername())
                        .fechaCreacion(o.getFechaCreacion())
                        .montoFinal(o.getMontoFinal())
                        .estado(o.getEstado())
                        .build())
                .toList();

        BigDecimal ventasTotales = ordenRepository.sumMontoFinal();

        // ── KPI semanal ───────────────────────────────────────────────────
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemanaActual   = hoy.minusDays(6);          // últimos 7 días
        LocalDate finSemanaAnterior    = hoy.minusDays(7);
        LocalDate inicioSemanaAnterior = hoy.minusDays(13);         // 7 días previos

        BigDecimal ventasSemanaActual   = ordenRepository.sumMontoFinalConfirmadasEntreFechas(inicioSemanaActual, hoy);
        BigDecimal ventasSemanaAnterior = ordenRepository.sumMontoFinalConfirmadasEntreFechas(inicioSemanaAnterior, finSemanaAnterior);

        if (ventasSemanaActual   == null) ventasSemanaActual   = BigDecimal.ZERO;
        if (ventasSemanaAnterior == null) ventasSemanaAnterior = BigDecimal.ZERO;

        Double crecimientoVentasPct = null;
        if (ventasSemanaAnterior.compareTo(BigDecimal.ZERO) > 0) {
            crecimientoVentasPct = ventasSemanaActual
                    .subtract(ventasSemanaAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(ventasSemanaAnterior, 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // ── Variantes con bajo stock ───────────────────────────────────────
        List<VarianteBajoStockResponse> bajoStock = varianteRepository.findVariantesBajoStock()
                .stream()
                .map(v -> VarianteBajoStockResponse.builder()
                        .id(v.getId())
                        .productoId(v.getProducto().getId())
                        .nombreProducto(v.getProducto().getNombre())
                        .color(v.getColor())
                        .talla(v.getTalla())
                        .stock(v.getStock())
                        .build())
                .toList();

        DashboardResponse dashboard = DashboardResponse.builder()
                .productosActivos(productoRepository.countByEstado(EstadoProducto.ACTIVO))
                .ordenesPendientes(ordenRepository.countByEstado(EstadoOrden.PENDIENTE))
                .descuentosActivos(descuentoRepository.countActivosVigentes(LocalDate.now()))
                .clientesRegistrados(usuarioRepository.countByRol(RolUsuario.CLIENTE))
                .ventasTotales(ventasTotales != null ? ventasTotales : BigDecimal.ZERO)
                .ordenesRecientes(ordenesRecientes)
                .ventasSemanaActual(ventasSemanaActual)
                .ventasSemanaAnterior(ventasSemanaAnterior)
                .crecimientoVentasPct(crecimientoVentasPct)
                .variantesBajoStock(bajoStock)
                .build();

        return ResponseEntity.ok(dashboard);
    }
}
