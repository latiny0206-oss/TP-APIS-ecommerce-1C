package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.ItemOrden;
import java.util.List;

public interface ItemOrdenService {
    List<ItemOrden> findAll();
    ItemOrden findById(Long id);
    ItemOrden create(ItemOrden itemOrden);
    ItemOrden update(Long id, ItemOrden itemOrden);
    void delete(Long id);
}

