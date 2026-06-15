package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.ContactoMensaje;

public interface MailService {

    /**
     * Envía notificación al admin y auto-respuesta al remitente.
     * @return true si los emails se enviaron, false si SMTP no está configurado o hubo error.
     */
    boolean enviarNotificaciones(ContactoMensaje mensaje);
}
