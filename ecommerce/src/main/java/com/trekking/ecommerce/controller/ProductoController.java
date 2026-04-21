package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.ProductoRequest;
import com.trekking.ecommerce.dto.ProductoResponse;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.service.ProductoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> findAll() {
        return ResponseEntity.ok(productoService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(productoService.findById(id)));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponse>> findByCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(productoService.findByCategoria(categoriaId).stream()
                .map(this::toResponse).toList());
    }

    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<List<ProductoResponse>> findByMarca(@PathVariable Long marcaId) {
        return ResponseEntity.ok(productoService.findByMarca(marcaId).stream()
                .map(this::toResponse).toList());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ProductoResponse>> findByEstado(@PathVariable EstadoProducto estado) {
        return ResponseEntity.ok(productoService.findByEstado(estado).stream()
                .map(this::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> create(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(productoService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> update(@PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(toResponse(productoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/disponible")
    public ResponseEntity<Boolean> estaDisponible(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.estaDisponible(id));
    }

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .marcaId(p.getMarca().getId())
                .marcaNombre(p.getMarca().getNombre())
                .categoriaId(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .estado(p.getEstado())
                .precioBase(p.getPrecioBase())
                .build();
    }
}
