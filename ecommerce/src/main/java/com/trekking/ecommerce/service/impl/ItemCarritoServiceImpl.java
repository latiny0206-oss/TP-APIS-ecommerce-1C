package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ItemCarritoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemCarritoServiceImpl implements ItemCarritoService {

    private final ItemCarritoRepository itemCarritoRepository;
    private final CarritoRepository carritoRepository;
    private final VarianteProductoRepository varianteProductoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarrito> findAll() {
        return itemCarritoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemCarrito findById(Long id) {
        return itemCarritoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCarrito no encontrado: " + id));
    }

    @Override
    public ItemCarrito create(ItemCarrito itemCarrito) {
        itemCarrito.setCarrito(carritoRepository.findById(itemCarrito.getCarrito().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado: " + itemCarrito.getCarrito().getId())));
        itemCarrito.setVariante(varianteProductoRepository.findById(itemCarrito.getVariante().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + itemCarrito.getVariante().getId())));
        return itemCarritoRepository.save(itemCarrito);
    }

    @Override
    public ItemCarrito update(Long id, ItemCarrito itemCarrito) {
        ItemCarrito actual = findById(id);
        actual.setCarrito(carritoRepository.findById(itemCarrito.getCarrito().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado: " + itemCarrito.getCarrito().getId())));
        actual.setVariante(varianteProductoRepository.findById(itemCarrito.getVariante().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + itemCarrito.getVariante().getId())));
        actual.setCantidad(itemCarrito.getCantidad());
        actual.setPrecioUnitario(itemCarrito.getPrecioUnitario());
        return itemCarritoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        itemCarritoRepository.deleteById(id);
    }
}

