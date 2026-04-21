package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ItemOrdenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemOrdenServiceImpl implements ItemOrdenService {

    private final ItemOrdenRepository itemOrdenRepository;
    private final OrdenRepository ordenRepository;
    private final VarianteProductoRepository varianteProductoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemOrden> findAll() {
        return itemOrdenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemOrden findById(Long id) {
        return itemOrdenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemOrden no encontrado: " + id));
    }

    @Override
    public ItemOrden create(ItemOrden itemOrden) {
        itemOrden.setOrden(ordenRepository.findById(itemOrden.getOrden().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + itemOrden.getOrden().getId())));
        itemOrden.setVariante(varianteProductoRepository.findById(itemOrden.getVariante().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + itemOrden.getVariante().getId())));
        return itemOrdenRepository.save(itemOrden);
    }

    @Override
    public ItemOrden update(Long id, ItemOrden itemOrden) {
        ItemOrden actual = findById(id);
        actual.setOrden(ordenRepository.findById(itemOrden.getOrden().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + itemOrden.getOrden().getId())));
        actual.setVariante(varianteProductoRepository.findById(itemOrden.getVariante().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + itemOrden.getVariante().getId())));
        actual.setCantidad(itemOrden.getCantidad());
        actual.setPrecioAlMomento(itemOrden.getPrecioAlMomento());
        return itemOrdenRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        itemOrdenRepository.deleteById(id);
    }
}

