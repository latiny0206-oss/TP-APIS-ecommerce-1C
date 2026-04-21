package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.ProductoRequest;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import java.util.List;

public interface ProductoService {
    List<Producto> findAll();
    List<Producto> findByCategoria(Long categoriaId);
    List<Producto> findByMarca(Long marcaId);
    List<Producto> findByEstado(EstadoProducto estado);
    Producto findById(Long id);
    Producto create(ProductoRequest request);
    Producto update(Long id, ProductoRequest request);
    void delete(Long id);
    boolean estaDisponible(Long id);
}
