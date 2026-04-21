package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.dto.VarianteProductoResponse;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.service.VarianteProductoService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/variantes")
@RequiredArgsConstructor
public class VarianteProductoController {

    private final VarianteProductoService varianteProductoService;

    @GetMapping
    public ResponseEntity<List<VarianteProductoResponse>> findAll() {
        return ResponseEntity.ok(varianteProductoService.findAll().stream()
                .map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteProductoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(varianteProductoService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarianteProductoResponse> create(
            @Valid @RequestBody VarianteProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(varianteProductoService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarianteProductoResponse> update(@PathVariable Long id,
            @Valid @RequestBody VarianteProductoRequest request) {
        return ResponseEntity.ok(toResponse(varianteProductoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        varianteProductoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/precio")
    public ResponseEntity<BigDecimal> getPrecio(@PathVariable Long id) {
        return ResponseEntity.ok(varianteProductoService.getPrecio(id));
    }

    @GetMapping("/{id}/stock/disponible")
    public ResponseEntity<Boolean> tieneStock(@PathVariable Long id,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(varianteProductoService.tieneStock(id, cantidad));
    }

    private VarianteProductoResponse toResponse(VarianteProducto v) {
        return VarianteProductoResponse.builder()
                .id(v.getId())
                .productoId(v.getProducto().getId())
                .productoNombre(v.getProducto().getNombre())
                .color(v.getColor())
                .talla(v.getTalla())
                .material(v.getMaterial())
                .peso(v.getPeso())
                .stock(v.getStock())
                .precio(v.getPrecio())
                .estacion(v.getEstacion())
                .build();
    }
}
