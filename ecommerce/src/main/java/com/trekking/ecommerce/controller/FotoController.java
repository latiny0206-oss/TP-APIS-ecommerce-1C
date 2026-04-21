package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.FotoResponse;
import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.service.FotoService;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final FotoService fotoService;

    @GetMapping
    public ResponseEntity<List<FotoResponse>> findAll() {
        return ResponseEntity.ok(fotoService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FotoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(fotoService.findById(id)));
    }

    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<FotoResponse>> findByVariante(@PathVariable Long varianteId) {
        return ResponseEntity.ok(fotoService.findByVariante(varianteId).stream()
                .map(this::toResponse).toList());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FotoResponse> create(
            @RequestParam Long varianteId,
            @RequestParam Integer orden,
            @RequestParam MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(fotoService.create(varianteId, orden, archivo)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FotoResponse> update(
            @PathVariable Long id,
            @RequestParam Long varianteId,
            @RequestParam Integer orden,
            @RequestParam MultipartFile archivo) {
        return ResponseEntity.ok(toResponse(fotoService.update(id, varianteId, orden, archivo)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fotoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private FotoResponse toResponse(Foto f) {
        String datosBase64 = f.getDatos() != null
                ? Base64.getEncoder().encodeToString(f.getDatos())
                : null;
        return FotoResponse.builder()
                .id(f.getId())
                .varianteId(f.getVariante().getId())
                .nombre(f.getNombre())
                .tipoContenido(f.getTipoContenido())
                .orden(f.getOrden())
                .datos(datosBase64)
                .build();
    }
}
