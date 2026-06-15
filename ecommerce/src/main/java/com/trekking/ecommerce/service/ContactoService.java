package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.ContactoRequest;
import com.trekking.ecommerce.model.ContactoMensaje;
import java.util.List;

public interface ContactoService {
    ContactoMensaje create(ContactoRequest request);
    List<ContactoMensaje> findAll();
}
