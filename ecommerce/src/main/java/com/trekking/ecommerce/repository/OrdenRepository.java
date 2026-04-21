package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Orden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioId(Long usuarioId);

    @Query("SELECT DISTINCT o FROM Orden o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto WHERE o.usuario.id = :usuarioId")
    List<Orden> findByUsuarioIdConItems(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT o FROM Orden o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.variante v LEFT JOIN FETCH v.producto")
    List<Orden> findAllConItems();
}

