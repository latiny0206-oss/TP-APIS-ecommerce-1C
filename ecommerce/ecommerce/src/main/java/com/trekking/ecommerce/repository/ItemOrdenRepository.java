package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.ItemOrden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {
	List<ItemOrden> findByOrdenId(Long ordenId);

	boolean existsByVarianteId(Long varianteId);
}

