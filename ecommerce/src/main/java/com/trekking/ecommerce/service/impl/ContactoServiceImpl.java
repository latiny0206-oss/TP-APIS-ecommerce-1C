package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.ContactoRequest;
import com.trekking.ecommerce.dto.ContactoResponse;
import com.trekking.ecommerce.model.ContactoMensaje;
import com.trekking.ecommerce.repository.ContactoRepository;
import com.trekking.ecommerce.service.ContactoService;
import com.trekking.ecommerce.service.MailService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoServiceImpl implements ContactoService {

    private final ContactoRepository contactoRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public ContactoResponse create(ContactoRequest request) {
        ContactoMensaje mensaje = ContactoMensaje.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .asunto(request.getAsunto())
                .mensaje(request.getMensaje())
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();
        ContactoMensaje guardado = contactoRepository.save(mensaje);

        // El mensaje ya está en BD; el email es best-effort
        boolean emailOk = mailService.enviarNotificaciones(guardado);

        return ContactoResponse.builder()
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .email(guardado.getEmail())
                .asunto(guardado.getAsunto())
                .mensaje(guardado.getMensaje())
                .fechaEnvio(guardado.getFechaEnvio())
                .leido(guardado.isLeido())
                .emailEnviado(emailOk)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactoMensaje> findAll() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc();
    }
}
