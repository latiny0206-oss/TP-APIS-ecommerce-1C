package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.CarritoRequest;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.Orden;
import java.math.BigDecimal;
import java.util.List;

public interface CarritoService {
    List<Carrito> findAll();
    List<Carrito> findAllConItems();
    List<Carrito> findByUsuario(Long usuarioId);
    List<Carrito> findByUsuarioConItems(Long usuarioId);
    Carrito findById(Long id);
    Carrito create(CarritoRequest request);
    Carrito update(Long id, CarritoRequest request);
    void delete(Long id);
    ItemCarrito agregarItem(Long idCarrito, Long idVariante, Integer cantidad);
    void eliminarItem(Long idCarrito, Long idItemCarrito);
    ItemCarrito actualizarItem(Long idCarrito, Long idItemCarrito, Integer cantidad);
    BigDecimal calcularTotal(Long idCarrito);
    void vaciarCarrito(Long idCarrito);
    List<ItemCarrito> obtenerItems(Long idCarrito);
    Orden realizarCompra(Long idCarrito);
    int vaciarCarritosAbandonados(int diasInactividad);
}
