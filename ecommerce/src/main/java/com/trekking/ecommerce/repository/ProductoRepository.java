package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Derived queries — used for counts and internal lookups
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByMarcaId(Long marcaId);
    List<Producto> findByEstado(EstadoProducto estado);
    List<Producto> findByEstadoAndCategoriaId(EstadoProducto estado, Long categoriaId);
    List<Producto> findByEstadoAndMarcaId(EstadoProducto estado, Long marcaId);
    long countByEstado(EstadoProducto estado);
    long countByCategoriaId(Long categoriaId);
    long countByMarcaId(Long marcaId);

    // JOIN FETCH variants — avoid N+1 when toResponse accesses marca/categoria
    @Query("SELECT p FROM Producto p JOIN FETCH p.marca JOIN FETCH p.categoria WHERE p.estado = :estado")
    List<Producto> findByEstadoFetch(@Param("estado") EstadoProducto estado);

    @Query("SELECT p FROM Producto p JOIN FETCH p.marca JOIN FETCH p.categoria WHERE p.estado = :estado AND p.categoria.id = :categoriaId")
    List<Producto> findByEstadoAndCategoriaIdFetch(@Param("estado") EstadoProducto estado, @Param("categoriaId") Long categoriaId);

    @Query("SELECT p FROM Producto p JOIN FETCH p.marca JOIN FETCH p.categoria WHERE p.estado = :estado AND p.marca.id = :marcaId")
    List<Producto> findByEstadoAndMarcaIdFetch(@Param("estado") EstadoProducto estado, @Param("marcaId") Long marcaId);
}
