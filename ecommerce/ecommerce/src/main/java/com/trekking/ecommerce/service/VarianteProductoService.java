package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.model.VarianteProducto;
import java.math.BigDecimal;
import java.util.List;

public interface VarianteProductoService {
    List<VarianteProducto> findAll();
    VarianteProducto findById(Long id);
    VarianteProducto create(VarianteProductoRequest request);
    VarianteProducto update(Long id, VarianteProductoRequest request);
    void delete(Long id);
    BigDecimal getPrecio(Long id);
    boolean tieneStock(Long id, Integer cantidad);
    VarianteProducto descontarStock(Long id, Integer cantidad);
    VarianteProducto restaurarStock(Long id, Integer cantidad);
}
