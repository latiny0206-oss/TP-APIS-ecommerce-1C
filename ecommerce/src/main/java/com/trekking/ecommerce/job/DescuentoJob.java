package com.trekking.ecommerce.job;

import com.trekking.ecommerce.service.DescuentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DescuentoJob {

    private final DescuentoService descuentoService;

    /**
     * Todos los días a las 00:05 AM marca como EXPIRADO cualquier descuento
     * que tenga estado ACTIVO pero cuya fechaFin sea anterior a hoy.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void expirarDescuentosVencidos() {
        log.info("[DescuentoJob] Verificando descuentos expirados");
        try {
            int procesados = descuentoService.expirarVencidos();
            log.info("[DescuentoJob] Descuentos marcados como EXPIRADO: {}", procesados);
        } catch (Exception e) {
            log.error("[DescuentoJob] Error al expirar descuentos: {}", e.getMessage(), e);
        }
    }
}
