package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.ItemCarrito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
	List<ItemCarrito> findByCarritoId(Long carritoId);

	Optional<ItemCarrito> findByCarritoIdAndVarianteId(Long carritoId, Long varianteId);

	boolean existsByVarianteId(Long varianteId);
}

