package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.MarcaRequest;
import com.trekking.ecommerce.dto.MarcaResponse;
import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.service.MarcaService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @GetMapping
    public ResponseEntity<List<MarcaResponse>> findAll() {
        List<MarcaResponse> list = marcaService.findAll().stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(marcaService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaResponse> create(@Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(marcaService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaResponse> update(@PathVariable Long id,
            @Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(toResponse(marcaService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        marcaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private MarcaResponse toResponse(Marca m) {
        return MarcaResponse.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .descripcion(m.getDescripcion())
                .build();
    }
}
