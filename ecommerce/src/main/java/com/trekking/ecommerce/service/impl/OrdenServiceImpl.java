package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.OrdenService;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final DescuentoRepository descuentoRepository;
    private final VarianteProductoService varianteProductoService;

    @Override
    @Transactional(readOnly = true)
    public List<Orden> findAll() {
        return ordenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orden> findAllConItems() {
        return ordenRepository.findAllConItems();
    }

    @Override
    @Transactional(readOnly = true)
    public Orden findById(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", id));
    }

    @Override
    @Transactional
    public Orden create(Orden orden) {
        orden.setUsuario(usuarioRepository.findById(orden.getUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", orden.getUsuario().getId())));
        if (orden.getCarrito() != null) {
            orden.setCarrito(carritoRepository.findById(orden.getCarrito().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carrito", orden.getCarrito().getId())));
        }
        if (orden.getDescuento() != null) {
            orden.setDescuento(descuentoRepository.findById(orden.getDescuento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Descuento", orden.getDescuento().getId())));
        }
        return ordenRepository.save(orden);
    }

    @Override
    @Transactional
    public Orden update(Long id, Orden orden) {
        Orden actual = findById(id);
        actual.setUsuario(usuarioRepository.findById(orden.getUsuario().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", orden.getUsuario().getId())));
        if (orden.getCarrito() != null) {
            actual.setCarrito(carritoRepository.findById(orden.getCarrito().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carrito", orden.getCarrito().getId())));
        } else {
            actual.setCarrito(null);
        }
        if (orden.getDescuento() != null) {
            actual.setDescuento(descuentoRepository.findById(orden.getDescuento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Descuento", orden.getDescuento().getId())));
        } else {
            actual.setDescuento(null);
        }
        actual.setFechaCreacion(orden.getFechaCreacion());
        actual.setMontoFinal(orden.getMontoFinal());
        actual.setEstado(orden.getEstado());
        return ordenRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Orden orden = findById(id);
        // Restaurar stock solo si la orden no fue entregada ni ya cancelada
        if (orden.getEstado() != EstadoOrden.ENTREGADA && orden.getEstado() != EstadoOrden.CANCELADA) {
            restaurarStockDeOrden(orden);
        }
        ordenRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Orden confirmar(Long id) {
        Orden orden = findById(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new BusinessRuleException("Solo se puede confirmar una orden en estado PENDIENTE. Estado actual: "
                    + orden.getEstado());
        }
        orden.setEstado(EstadoOrden.CONFIRMADA);
        return ordenRepository.save(orden);
    }

    @Override
    @Transactional
    public Orden cancelar(Long id) {
        Orden orden = findById(id);
        if (orden.getEstado() == EstadoOrden.ENTREGADA || orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new BusinessRuleException("No se puede cancelar una orden en estado " + orden.getEstado());
        }
        restaurarStockDeOrden(orden);
        orden.setEstado(EstadoOrden.CANCELADA);
        return ordenRepository.save(orden);
    }

    private void restaurarStockDeOrden(Orden orden) {
        itemOrdenRepository.findByOrdenId(orden.getId())
                .forEach(item -> varianteProductoService.restaurarStock(
                        item.getVariante().getId(), item.getCantidad()));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMontoFinal(Long id) {
        return findById(id).getMontoFinal();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemOrden> obtenerItems(Long id) {
        findById(id);
        return itemOrdenRepository.findByOrdenId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orden> findByUsuario(Long idUsuario) {
        return ordenRepository.findByUsuarioId(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orden> findByUsuarioConItems(Long idUsuario) {
        return ordenRepository.findByUsuarioIdConItems(idUsuario);
    }
}
