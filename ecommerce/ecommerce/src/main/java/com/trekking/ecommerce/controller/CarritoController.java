package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.CarritoRequest;
import com.trekking.ecommerce.dto.CarritoResponse;
import com.trekking.ecommerce.dto.ItemCarritoRequest;
import com.trekking.ecommerce.dto.ItemCarritoResponse;
import com.trekking.ecommerce.dto.OrdenResponse;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.service.CarritoService;
import com.trekking.ecommerce.service.OrdenService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carritos")
@RequiredArgsConstructor
public class CarritoController extends AuthenticatedController {

    private final CarritoService carritoService;
    private final OrdenService ordenService;

    @GetMapping
    public ResponseEntity<List<CarritoResponse>> findAll() {
        List<Carrito> carritos = esAdmin()
                ? carritoService.findAllConItems()
                : carritoService.findByUsuarioConItems(getUsuarioAutenticado().getId());
        return ResponseEntity.ok(carritos.stream().map(c -> toResponse(c, c.getItems())).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponse> findById(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        validarPropietario(carrito.getUsuario().getId());
        return ResponseEntity.ok(toResponse(carrito));
    }

    @PostMapping
    public ResponseEntity<CarritoResponse> create(@Valid @RequestBody CarritoRequest request) {
        request.setUsuarioId(getUsuarioAutenticado().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(carritoService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoResponse> update(@PathVariable Long id,
            @Valid @RequestBody CarritoRequest request) {
        Carrito carrito = carritoService.findById(id);
        validarPropietario(carrito.getUsuario().getId());
        request.setUsuarioId(carrito.getUsuario().getId());
        return ResponseEntity.ok(toResponse(carritoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        carritoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItemCarritoResponse> agregarItem(
            @PathVariable Long id,
            @RequestBody @Valid ItemCarritoRequest request) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toItemResponse(
                carritoService.agregarItem(id, request.getIdVariante(), request.getCantidad())));
    }

    @DeleteMapping("/{id}/items/{idItem}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id, @PathVariable Long idItem) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        carritoService.eliminarItem(id, idItem);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/items/{idItem}")
    public ResponseEntity<ItemCarritoResponse> actualizarItem(
            @PathVariable Long id,
            @PathVariable Long idItem,
            @RequestParam Integer cantidad) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toItemResponse(carritoService.actualizarItem(id, idItem, cantidad)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemCarritoResponse>> obtenerItems(@PathVariable Long id) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(carritoService.obtenerItems(id).stream()
                .map(this::toItemResponse).toList());
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotal(@PathVariable Long id) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(carritoService.calcularTotal(id));
    }

    @PostMapping("/{id}/vaciar")
    public ResponseEntity<Void> vaciar(@PathVariable Long id) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        carritoService.vaciarCarrito(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<OrdenResponse> realizarCompra(@PathVariable Long id) {
        validarPropietario(carritoService.findById(id).getUsuario().getId());
        Orden orden = carritoService.realizarCompra(id);
        return ResponseEntity.ok(toOrdenResponse(orden));
    }

    // ─── Mapeo a DTOs ────────────────────────────────────────────────────────

    private CarritoResponse toResponse(Carrito c) {
        return toResponse(c, carritoService.obtenerItems(c.getId()));
    }

    private CarritoResponse toResponse(Carrito c, List<ItemCarrito> items) {
        return CarritoResponse.builder()
                .id(c.getId())
                .usuarioId(c.getUsuario().getId())
                .usuarioUsername(c.getUsuario().getUsername())
                .descuentoId(c.getDescuento() != null ? c.getDescuento().getId() : null)
                .estado(c.getEstado())
                .montoTotal(c.getMontoTotal())
                .fechaUltimaModificacion(c.getFechaUltimaModificacion())
                .items(items.stream().map(this::toItemResponse).toList())
                .build();
    }

    private ItemCarritoResponse toItemResponse(ItemCarrito item) {
        return ItemCarritoResponse.builder()
                .id(item.getId())
                .varianteId(item.getVariante().getId())
                .varianteColor(item.getVariante().getColor())
                .varianteTalla(item.getVariante().getTalla())
                .productoNombre(item.getVariante().getProducto().getNombre())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .build();
    }

    private OrdenResponse toOrdenResponse(Orden o) {
        List<com.trekking.ecommerce.dto.ItemOrdenResponse> items =
                ordenService.obtenerItems(o.getId()).stream()
                        .map(item -> com.trekking.ecommerce.dto.ItemOrdenResponse.builder()
                                .id(item.getId())
                                .varianteId(item.getVariante().getId())
                                .varianteColor(item.getVariante().getColor())
                                .varianteTalla(item.getVariante().getTalla())
                                .productoNombre(item.getVariante().getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .precioAlMomento(item.getPrecioAlMomento())
                                .build())
                        .toList();
        return OrdenResponse.builder()
                .id(o.getId())
                .usuarioId(o.getUsuario().getId())
                .carritoId(o.getCarrito() != null ? o.getCarrito().getId() : null)
                .descuentoId(o.getDescuento() != null ? o.getDescuento().getId() : null)
                .fechaCreacion(o.getFechaCreacion())
                .montoFinal(o.getMontoFinal())
                .estado(o.getEstado())
                .items(items)
                .build();
    }
}
