package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.ItemOrdenResponse;
import com.trekking.ecommerce.dto.OrdenRequest;
import com.trekking.ecommerce.dto.OrdenResponse;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.service.OrdenService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController extends AuthenticatedController {

    private final OrdenService ordenService;

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> findAll() {
        List<Orden> ordenes = esAdmin()
                ? ordenService.findAllConItems()
                : ordenService.findByUsuarioConItems(getUsuarioAutenticado().getId());
        return ResponseEntity.ok(ordenes.stream().map(o -> toResponse(o, o.getItems())).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> findById(@PathVariable Long id) {
        Orden orden = ordenService.findById(id);
        validarPropietario(orden.getUsuario().getId());
        return ResponseEntity.ok(toResponse(orden));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenResponse> create(@Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ordenService.create(toEntity(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenResponse> update(@PathVariable Long id, @Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.ok(toResponse(ordenService.update(id, toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenResponse> confirmar(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toResponse(ordenService.confirmar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<OrdenResponse> cancelar(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toResponse(ordenService.cancelar(id)));
    }

    @GetMapping("/{id}/monto-final")
    public ResponseEntity<BigDecimal> getMontoFinal(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(ordenService.getMontoFinal(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemOrdenResponse>> obtenerItems(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(ordenService.obtenerItems(id).stream()
                .map(this::toItemResponse).toList());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<OrdenResponse>> historialPorUsuario(@PathVariable Long idUsuario) {
        validarPropietario(idUsuario);
        return ResponseEntity.ok(ordenService.findByUsuarioConItems(idUsuario).stream()
                .map(o -> toResponse(o, o.getItems())).toList());
    }

    // ─── Mapeo a DTOs / Entidades ────────────────────────────────────────────

    private Orden toEntity(OrdenRequest request) {
        Usuario usuario = new Usuario();
        usuario.setId(request.getUsuarioId());
        Carrito carrito = request.getCarritoId() != null ? new Carrito() : null;
        if (carrito != null) carrito.setId(request.getCarritoId());
        Descuento descuento = request.getDescuentoId() != null ? new Descuento() : null;
        if (descuento != null) descuento.setId(request.getDescuentoId());
        return Orden.builder()
                .usuario(usuario)
                .carrito(carrito)
                .descuento(descuento)
                .fechaCreacion(request.getFechaCreacion())
                .montoFinal(request.getMontoFinal())
                .estado(request.getEstado())
                .build();
    }

    // ─── Mapeo a DTOs ────────────────────────────────────────────────────────

    private OrdenResponse toResponse(Orden o) {
        return toResponse(o, ordenService.obtenerItems(o.getId()));
    }

    private OrdenResponse toResponse(Orden o, List<ItemOrden> items) {
        return OrdenResponse.builder()
                .id(o.getId())
                .usuarioId(o.getUsuario().getId())
                .carritoId(o.getCarrito() != null ? o.getCarrito().getId() : null)
                .descuentoId(o.getDescuento() != null ? o.getDescuento().getId() : null)
                .fechaCreacion(o.getFechaCreacion())
                .montoFinal(o.getMontoFinal())
                .estado(o.getEstado())
                .items(items.stream().map(this::toItemResponse).toList())
                .build();
    }

    private ItemOrdenResponse toItemResponse(ItemOrden item) {
        return ItemOrdenResponse.builder()
                .id(item.getId())
                .varianteId(item.getVariante().getId())
                .varianteColor(item.getVariante().getColor())
                .varianteTalla(item.getVariante().getTalla())
                .productoNombre(item.getVariante().getProducto().getNombre())
                .cantidad(item.getCantidad())
                .precioAlMomento(item.getPrecioAlMomento())
                .build();
    }
}
