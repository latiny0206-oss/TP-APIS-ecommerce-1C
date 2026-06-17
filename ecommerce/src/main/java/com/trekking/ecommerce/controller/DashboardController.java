package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.DashboardResponse;
import com.trekking.ecommerce.dto.OrdenResumenResponse;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import java.math.BigDecimal;
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

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard() {
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

        DashboardResponse dashboard = DashboardResponse.builder()
                .productosActivos(productoRepository.countByEstado(EstadoProducto.ACTIVO))
                .ordenesPendientes(ordenRepository.countByEstado(EstadoOrden.PENDIENTE))
                .descuentosActivos(descuentoRepository.countActivosVigentes(LocalDate.now()))
                .clientesRegistrados(usuarioRepository.countByRol(RolUsuario.CLIENTE))
                .ventasTotales(ventasTotales != null ? ventasTotales : BigDecimal.ZERO)
                .ordenesRecientes(ordenesRecientes)
                .build();

        return ResponseEntity.ok(dashboard);
    }
}
