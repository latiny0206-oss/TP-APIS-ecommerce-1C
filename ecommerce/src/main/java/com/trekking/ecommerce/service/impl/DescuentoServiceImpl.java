package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.service.DescuentoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DescuentoServiceImpl implements DescuentoService {

    private final DescuentoRepository descuentoRepository;
    private final CarritoRepository carritoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> findAll() {
        return descuentoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Descuento> findActivos() {
        return descuentoRepository.findActivosVigentes(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Descuento findById(Long id) {
        return descuentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Descuento findByCodigo(String codigo) {
        return descuentoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un cupón con código: " + codigo));
    }

    @Override
    @Transactional
    public Descuento create(DescuentoRequest request) {
        if (descuentoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessRuleException("Ya existe un descuento con el nombre: " + request.getNombre());
        }
        validarReglas(request);
        Descuento descuento = Descuento.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado())
                .build();
        return descuentoRepository.save(descuento);
    }

    @Override
    @Transactional
    public Descuento update(Long id, DescuentoRequest request) {
        if (descuentoRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre(), id)) {
            throw new BusinessRuleException("Ya existe otro descuento con el nombre: " + request.getNombre());
        }
        validarReglas(request);
        Descuento actual = findById(id);
        actual.setNombre(request.getNombre());
        actual.setCodigo(request.getCodigo());
        actual.setTipo(request.getTipo());
        actual.setValor(request.getValor());
        actual.setFechaInicio(request.getFechaInicio());
        actual.setFechaFin(request.getFechaFin());
        actual.setEstado(request.getEstado());
        return descuentoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (carritoRepository.existsByDescuentoIdAndEstado(id, EstadoCarrito.ACTIVO)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el descuento id " + id
                            + " porque está siendo usado por uno o más carritos activos");
        }
        descuentoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularDescuento(Long id, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El monto para calcular descuento debe ser mayor o igual a 0");
        }
        Descuento descuento = findById(id);
        if (!estaVigente(id)) {
            return BigDecimal.ZERO;
        }
        if (descuento.getTipo() == TipoDescuento.FIJO) {
            return descuento.getValor().min(monto);
        }
        return monto.multiply(descuento.getValor()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaVigente(Long id) {
        Descuento descuento = findById(id);
        LocalDate hoy = LocalDate.now();
        return descuento.getEstado() == EstadoDescuento.ACTIVO
                && !hoy.isBefore(descuento.getFechaInicio())
                && !hoy.isAfter(descuento.getFechaFin());
    }

    @Override
    @Transactional
    public int expirarVencidos() {
        List<Descuento> expirados = descuentoRepository.findByEstadoAndFechaFinBefore(EstadoDescuento.ACTIVO, LocalDate.now());
        expirados.forEach(d -> d.setEstado(EstadoDescuento.EXPIRADO));
        descuentoRepository.saveAll(expirados);
        return expirados.size();
    }

    private void validarReglas(DescuentoRequest request) {
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BusinessRuleException(
                    "La fecha de fin debe ser posterior o igual a la fecha de inicio");
        }
        if (request.getTipo() == TipoDescuento.PORCENTAJE) {
            if (request.getValor().compareTo(BigDecimal.ZERO) <= 0
                    || request.getValor().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessRuleException("El valor del descuento porcentual debe ser mayor a 0 y máximo 100");
            }
        }
        if (request.getTipo() == TipoDescuento.FIJO
                && request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                    "El valor del descuento fijo debe ser mayor a 0");
        }
    }
}
