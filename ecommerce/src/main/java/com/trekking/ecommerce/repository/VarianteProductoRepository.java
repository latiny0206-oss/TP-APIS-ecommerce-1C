package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.VarianteProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {
	List<VarianteProducto> findByProductoId(Long productoId);
}

