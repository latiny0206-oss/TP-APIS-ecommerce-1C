package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.ContactoMensaje;
import com.trekking.ecommerce.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    // Null cuando SMTP no está configurado (spring.mail.host vacío)
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@cumbre.com}")
    private String mailFrom;

    @Value("${contact.email.to:contacto@cumbre.com}")
    private String contactTo;

    @Override
    public boolean enviarNotificaciones(ContactoMensaje mensaje) {
        if (mailSender == null) {
            log.info("SMTP no configurado. Mensaje id={} guardado en BD sin enviar email.", mensaje.getId());
            return false;
        }
        try {
            enviarAlAdmin(mensaje);
            enviarAutoRespuesta(mensaje);
            log.info("Email de contacto enviado correctamente para id={}", mensaje.getId());
            return true;
        } catch (Exception e) {
            log.error("Error al enviar email para contacto id={}: {}", mensaje.getId(), e.getMessage());
            return false;
        }
    }

    private void enviarAlAdmin(ContactoMensaje m) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(contactTo);
        msg.setReplyTo(m.getEmail());
        msg.setSubject("[Contacto Web] " + (m.getAsunto() != null && !m.getAsunto().isBlank()
                ? m.getAsunto() : "Sin asunto"));
        msg.setText(String.format(
                "Nuevo mensaje de contacto recibido en Cumbre Trekking.\n\n" +
                "Nombre:  %s\n" +
                "Email:   %s\n" +
                "Asunto:  %s\n\n" +
                "Mensaje:\n%s",
                m.getNombre(), m.getEmail(),
                m.getAsunto() != null ? m.getAsunto() : "-",
                m.getMensaje()));
        mailSender.send(msg);
    }

    private void enviarAutoRespuesta(ContactoMensaje m) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(m.getEmail());
        msg.setSubject("Recibimos tu consulta - Cumbre Trekking");
        msg.setText(String.format(
                "Hola %s,\n\n" +
                "Recibimos tu mensaje y nos pondremos en contacto a la brevedad.\n\n" +
                "Asunto: %s\n\n" +
                "Gracias por contactarte con Cumbre Trekking.\n" +
                "Equipo Cumbre Trekking",
                m.getNombre(),
                m.getAsunto() != null ? m.getAsunto() : "-"));
        mailSender.send(msg);
    }
}
