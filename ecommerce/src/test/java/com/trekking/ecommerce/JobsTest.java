package com.trekking.ecommerce;

import com.trekking.ecommerce.job.CarritoJob;
import com.trekking.ecommerce.job.DescuentoJob;
import com.trekking.ecommerce.service.CarritoService;
import com.trekking.ecommerce.service.DescuentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobsTest {

    // ─────────────────────────────────────────────────────────────────────────
    // CarritoJob
    // ─────────────────────────────────────────────────────────────────────────

    @Mock
    private CarritoService carritoService;

    @InjectMocks
    private CarritoJob carritoJob;

    @Test
    void carritoJob_vaciarInactivos_invocaServicio() {
        when(carritoService.vaciarCarritosAbandonados(7)).thenReturn(3);

        carritoJob.vaciarCarritosInactivos();

        verify(carritoService).vaciarCarritosAbandonados(7);
    }

    @Test
    void carritoJob_excepciones_noSePropagan() {
        doThrow(new RuntimeException("DB error")).when(carritoService).vaciarCarritosAbandonados(anyInt());

        assertThatNoException().isThrownBy(() -> carritoJob.vaciarCarritosInactivos());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DescuentoJob
    // ─────────────────────────────────────────────────────────────────────────

    @Mock
    private DescuentoService descuentoService;

    @InjectMocks
    private DescuentoJob descuentoJob;

    @Test
    void descuentoJob_expirarVencidos_invocaServicio() {
        when(descuentoService.expirarVencidos()).thenReturn(2);

        descuentoJob.expirarDescuentosVencidos();

        verify(descuentoService).expirarVencidos();
    }

    @Test
    void descuentoJob_excepciones_noSePropagan() {
        doThrow(new RuntimeException("error")).when(descuentoService).expirarVencidos();

        assertThatNoException().isThrownBy(() -> descuentoJob.expirarDescuentosVencidos());
    }
}
