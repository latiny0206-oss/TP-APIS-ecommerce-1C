package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import java.math.BigDecimal;
import java.util.List;

public interface OrdenService {
    List<Orden> findAll();
    List<Orden> findAllConItems();
    Orden findById(Long id);
    Orden create(Orden orden);
    Orden update(Long id, Orden orden);
    void delete(Long id);
    Orden confirmar(Long id);
    Orden cancelar(Long id);
    BigDecimal getMontoFinal(Long id);
    List<ItemOrden> obtenerItems(Long id);
    List<Orden> findByUsuario(Long idUsuario);
    List<Orden> findByUsuarioConItems(Long idUsuario);
}

