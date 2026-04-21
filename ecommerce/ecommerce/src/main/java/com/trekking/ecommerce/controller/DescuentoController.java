package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.dto.DescuentoResponse;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.service.DescuentoService;
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
@RequestMapping("/api/descuentos")
@RequiredArgsConstructor
public class DescuentoController {

    private final DescuentoService descuentoService;

    @GetMapping
    public ResponseEntity<List<DescuentoResponse>> findAll() {
        return ResponseEntity.ok(descuentoService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DescuentoResponse>> findActivos() {
        return ResponseEntity.ok(descuentoService.findActivos().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DescuentoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(descuentoService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentoResponse> create(@Valid @RequestBody DescuentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(descuentoService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentoResponse> update(@PathVariable Long id,
            @Valid @RequestBody DescuentoRequest request) {
        return ResponseEntity.ok(toResponse(descuentoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        descuentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vigente")
    public ResponseEntity<Boolean> estaVigente(@PathVariable Long id) {
        return ResponseEntity.ok(descuentoService.estaVigente(id));
    }

    @GetMapping("/{id}/calcular")
    public ResponseEntity<BigDecimal> calcular(@PathVariable Long id, @RequestParam BigDecimal monto) {
        return ResponseEntity.ok(descuentoService.calcularDescuento(id, monto));
    }

    private DescuentoResponse toResponse(Descuento d) {
        return DescuentoResponse.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .tipo(d.getTipo())
                .valor(d.getValor())
                .fechaInicio(d.getFechaInicio())
                .fechaFin(d.getFechaFin())
                .estado(d.getEstado())
                .build();
    }
}
