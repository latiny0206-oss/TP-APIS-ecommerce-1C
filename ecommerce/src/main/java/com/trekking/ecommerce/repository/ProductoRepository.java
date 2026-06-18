package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByMarcaId(Long marcaId);

    List<Producto> findByEstado(EstadoProducto estado);

    List<Producto> findByEstadoAndCategoriaId(EstadoProducto estado, Long categoriaId);

    List<Producto> findByEstadoAndMarcaId(EstadoProducto estado, Long marcaId);

    long countByEstado(EstadoProducto estado);

    long countByCategoriaId(Long categoriaId);

    long countByMarcaId(Long marcaId);
}
