package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioId(Long usuarioId);

    @Query("SELECT DISTINCT o FROM Orden o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto WHERE o.usuario.id = :usuarioId")
    List<Orden> findByUsuarioIdConItems(Long usuarioId);

    @Query("SELECT DISTINCT o FROM Orden o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto")
    List<Orden> findAllConItems();

    long countByEstado(EstadoOrden estado);

    List<Orden> findTop5ByOrderByFechaCreacionDesc();

    @Query("SELECT COALESCE(SUM(o.montoFinal), 0) FROM Orden o")
    BigDecimal sumMontoFinal();
}
