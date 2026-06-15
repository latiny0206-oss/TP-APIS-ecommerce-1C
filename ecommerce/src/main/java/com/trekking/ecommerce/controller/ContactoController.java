package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.ContactoRequest;
import com.trekking.ecommerce.dto.ContactoResponse;
import com.trekking.ecommerce.model.ContactoMensaje;
import com.trekking.ecommerce.service.ContactoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    @PostMapping
    public ResponseEntity<ContactoResponse> enviar(@Valid @RequestBody ContactoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactoService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactoResponse>> findAll() {
        return ResponseEntity.ok(contactoService.findAll().stream().map(this::toResponse).toList());
    }

    private ContactoResponse toResponse(ContactoMensaje m) {
        return ContactoResponse.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .email(m.getEmail())
                .asunto(m.getAsunto())
                .mensaje(m.getMensaje())
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.isLeido())
                .emailEnviado(false)
                .build();
    }
}
