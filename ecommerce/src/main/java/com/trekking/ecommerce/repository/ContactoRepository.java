package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.ContactoMensaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<ContactoMensaje, Long> {

    List<ContactoMensaje> findAllByOrderByFechaEnvioDesc();
}
