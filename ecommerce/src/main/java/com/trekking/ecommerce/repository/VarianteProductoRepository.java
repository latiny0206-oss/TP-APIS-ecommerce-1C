package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.VarianteProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {
	List<VarianteProducto> findByProductoId(Long productoId);

	@Query("SELECT v FROM VarianteProducto v WHERE v.stock > 0 AND v.stock <= 3 ORDER BY v.stock ASC")
	List<VarianteProducto> findVariantesBajoStock();
}
