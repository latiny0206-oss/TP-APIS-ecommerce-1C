package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.model.Descuento;
import java.math.BigDecimal;
import java.util.List;

public interface DescuentoService {
    List<Descuento> findAll();
    List<Descuento> findActivos();
    Descuento findById(Long id);
    Descuento create(DescuentoRequest request);
    Descuento update(Long id, DescuentoRequest request);
    void delete(Long id);
    BigDecimal calcularDescuento(Long id, BigDecimal monto);
    boolean estaVigente(Long id);
    int expirarVencidos();
}
