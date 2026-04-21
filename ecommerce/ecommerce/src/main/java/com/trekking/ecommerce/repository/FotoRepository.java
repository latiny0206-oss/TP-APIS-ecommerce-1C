package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Foto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByVarianteId(Long varianteId);
}
