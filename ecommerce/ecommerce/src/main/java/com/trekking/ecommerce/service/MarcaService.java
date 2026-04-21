package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.MarcaRequest;
import com.trekking.ecommerce.model.Marca;
import java.util.List;

public interface MarcaService {
    List<Marca> findAll();
    Marca findById(Long id);
    Marca create(MarcaRequest request);
    Marca update(Long id, MarcaRequest request);
    void delete(Long id);
}
