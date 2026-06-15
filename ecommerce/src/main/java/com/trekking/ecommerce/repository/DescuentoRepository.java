package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Descuento> findByEstado(EstadoDescuento estado);

    List<Descuento> findByEstadoAndFechaFinBefore(EstadoDescuento estado, LocalDate hoy);

    Optional<Descuento> findByCodigoIgnoreCase(String codigo);

    @Query("SELECT d FROM Descuento d WHERE d.estado = 'ACTIVO' AND d.fechaInicio <= :hoy AND d.fechaFin >= :hoy")
    List<Descuento> findActivosVigentes(@Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(d) FROM Descuento d WHERE d.estado = 'ACTIVO' AND d.fechaInicio <= :hoy AND d.fechaFin >= :hoy")
    long countActivosVigentes(@Param("hoy") LocalDate hoy);
}
