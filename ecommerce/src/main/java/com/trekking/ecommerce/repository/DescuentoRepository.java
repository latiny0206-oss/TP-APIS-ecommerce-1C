package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    List<Descuento> findByEstado(EstadoDescuento estado);

    List<Descuento> findByEstadoAndFechaFinBefore(EstadoDescuento estado, LocalDate hoy);
}
