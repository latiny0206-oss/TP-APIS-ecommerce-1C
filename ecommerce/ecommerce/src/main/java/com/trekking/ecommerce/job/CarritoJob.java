package com.trekking.ecommerce.job;

import com.trekking.ecommerce.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarritoJob {

    private static final int DIAS_INACTIVIDAD = 7;

    private final CarritoService carritoService;

    /**
     * Todos los lunes a las 02:00 AM vacía los carritos ACTIVOS
     * que no hayan tenido actividad en los últimos 7 días.
     * Los marca como ABANDONADO y elimina sus ítems.
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void vaciarCarritosInactivos() {
        log.info("[CarritoJob] Iniciando limpieza de carritos inactivos (threshold: {} dias)", DIAS_INACTIVIDAD);
        try {
            int procesados = carritoService.vaciarCarritosAbandonados(DIAS_INACTIVIDAD);
            log.info("[CarritoJob] Limpieza completada. Carritos marcados como ABANDONADO: {}", procesados);
        } catch (Exception e) {
            log.error("[CarritoJob] Error durante la limpieza de carritos: {}", e.getMessage(), e);
        }
    }
}
