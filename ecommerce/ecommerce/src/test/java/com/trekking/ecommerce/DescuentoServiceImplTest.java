package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.service.impl.DescuentoServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescuentoServiceImplTest {

    @Mock private DescuentoRepository descuentoRepository;
    @Mock private CarritoRepository carritoRepository;

    @InjectMocks
    private DescuentoServiceImpl descuentoService;

    // ─────────────────────────────────────────────────────────────────────────
    // validarReglas — create
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void create_conFechaFinAntesDeInicio_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.FIJO);
        request.setFechaInicio(LocalDate.now().plusDays(5));
        request.setFechaFin(LocalDate.now());

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fecha de fin");
    }

    @Test
    void create_conTipoPorcentajeYValorCero_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.PORCENTAJE);
        request.setValor(BigDecimal.ZERO);

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("máximo 100");
    }

    @Test
    void create_conTipoPorcentajeYValorMayorA100_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.PORCENTAJE);
        request.setValor(new BigDecimal("150"));

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("máximo 100");
    }

    @Test
    void create_conTipoFijoYValorCero_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.FIJO);
        request.setValor(BigDecimal.ZERO);

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("mayor a 0");
    }

    @Test
    void create_valido_guardaYRetornaDescuento() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.FIJO);
        Descuento guardado = Descuento.builder()
                .id(1L)
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado())
                .build();
        when(descuentoRepository.save(any())).thenReturn(guardado);

        Descuento resultado = descuentoService.create(request);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo(request.getNombre());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calcularDescuento
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void calcularDescuento_fijo_menorAlMonto_devuelveElValorFijo() {
        Long id = 1L;
        Descuento d = descuentoActivo(TipoDescuento.FIJO, new BigDecimal("30.00"));
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("200.00"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void calcularDescuento_fijo_mayorAlMonto_devuelveElMontoCompleto() {
        Long id = 2L;
        Descuento d = descuentoActivo(TipoDescuento.FIJO, new BigDecimal("500.00"));
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("100.00"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void calcularDescuento_porcentaje_calculaCorrectamente() {
        Long id = 3L;
        Descuento d = descuentoActivo(TipoDescuento.PORCENTAJE, new BigDecimal("20"));
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("150.00"));

        // 150 * 20 / 100 = 30
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void calcularDescuento_descuentoExpirado_devuelveCero() {
        Long id = 4L;
        Descuento d = Descuento.builder()
                .id(id)
                .tipo(TipoDescuento.FIJO)
                .valor(new BigDecimal("50.00"))
                .estado(EstadoDescuento.EXPIRADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaFin(LocalDate.now().minusDays(1))
                .build();
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("200.00"));

        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void estaVigente_descuentoActivoEnRango_retornaTrue() {
        Long id = 5L;
        Descuento d = descuentoActivo(TipoDescuento.FIJO, new BigDecimal("10.00"));
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        assertThat(descuentoService.estaVigente(id)).isTrue();
    }

    @Test
    void estaVigente_descuentoExpirado_retornaFalse() {
        Long id = 6L;
        Descuento d = Descuento.builder()
                .id(id)
                .tipo(TipoDescuento.FIJO)
                .valor(new BigDecimal("10.00"))
                .estado(EstadoDescuento.EXPIRADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaFin(LocalDate.now().minusDays(1))
                .build();
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        assertThat(descuentoService.estaVigente(id)).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private DescuentoRequest descuentoRequestBase(TipoDescuento tipo) {
        DescuentoRequest req = new DescuentoRequest();
        req.setNombre("Descuento Test");
        req.setTipo(tipo);
        req.setValor(new BigDecimal("50.00"));
        req.setFechaInicio(LocalDate.now());
        req.setFechaFin(LocalDate.now().plusDays(30));
        req.setEstado(EstadoDescuento.ACTIVO);
        return req;
    }

    private Descuento descuentoActivo(TipoDescuento tipo, BigDecimal valor) {
        return Descuento.builder()
                .tipo(tipo)
                .valor(valor)
                .estado(EstadoDescuento.ACTIVO)
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(1))
                .build();
    }
}
