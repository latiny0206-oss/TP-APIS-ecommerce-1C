package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.FotoMetadata;
import com.trekking.ecommerce.dto.FotoResponse;
import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.service.FotoService;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
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

    // Metadata liviana (sin el binario en base64) para listados
    @GetMapping
    public ResponseEntity<List<FotoMetadata>> findAll() {
        return ResponseEntity.ok(fotoService.findAll().stream().map(this::toMetadata).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FotoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(fotoService.findById(id)));
    }

    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<List<FotoMetadata>> findByVariante(@PathVariable Long varianteId) {
        return ResponseEntity.ok(fotoService.findByVariante(varianteId).stream()
                .map(this::toMetadata).toList());
    }

    // Binario servido por separado: permite que el browser lo cachee por id
    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id) {
        Foto f = fotoService.findById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.getTipoContenido()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(f.getDatos());
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

    private FotoMetadata toMetadata(Foto f) {
        return FotoMetadata.builder()
                .id(f.getId())
                .varianteId(f.getVariante().getId())
                .nombre(f.getNombre())
                .tipoContenido(f.getTipoContenido())
                .orden(f.getOrden())
                .build();
    }
}
