package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.ItemCarrito;
import java.util.List;

public interface ItemCarritoService {
    List<ItemCarrito> findAll();
    ItemCarrito findById(Long id);
    ItemCarrito create(ItemCarrito itemCarrito);
    ItemCarrito update(Long id, ItemCarrito itemCarrito);
    void delete(Long id);
}

