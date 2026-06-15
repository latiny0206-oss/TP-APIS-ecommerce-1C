package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.ContactoRequest;
import com.trekking.ecommerce.model.ContactoMensaje;
import com.trekking.ecommerce.repository.ContactoRepository;
import com.trekking.ecommerce.service.ContactoService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoServiceImpl implements ContactoService {

    private final ContactoRepository contactoRepository;

    @Override
    @Transactional
    public ContactoMensaje create(ContactoRequest request) {
        ContactoMensaje mensaje = ContactoMensaje.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .asunto(request.getAsunto())
                .mensaje(request.getMensaje())
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();
        return contactoRepository.save(mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoMensaje> findAll() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc();
    }
}
