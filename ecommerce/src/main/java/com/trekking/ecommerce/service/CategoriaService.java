package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.CategoriaRequest;
import com.trekking.ecommerce.model.Categoria;
import java.util.List;

public interface CategoriaService {
    List<Categoria> findAll();
    Categoria findById(Long id);
    Categoria create(CategoriaRequest request);
    Categoria update(Long id, CategoriaRequest request);
    void delete(Long id);
}
