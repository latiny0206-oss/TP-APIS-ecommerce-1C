package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);

    @Query("SELECT DISTINCT c FROM Carrito c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto WHERE c.usuario.id = :usuarioId")
    List<Carrito> findByUsuarioIdConItems(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT c FROM Carrito c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto")
    List<Carrito> findAllConItems();

    Optional<Carrito> findByUsuarioIdAndEstado(Long usuarioId, EstadoCarrito estado);

    List<Carrito> findByEstadoAndFechaUltimaModificacionBefore(EstadoCarrito estado, LocalDateTime limite);

    boolean existsByDescuentoIdAndEstado(Long descuentoId, EstadoCarrito estado);
}
