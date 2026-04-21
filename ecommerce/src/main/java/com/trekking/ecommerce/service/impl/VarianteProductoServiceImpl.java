package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ProductoService;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VarianteProductoServiceImpl implements VarianteProductoService {

    private final VarianteProductoRepository varianteProductoRepository;
    private final ProductoService productoService;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ItemOrdenRepository itemOrdenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProducto> findAll() {
        return varianteProductoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteProducto findById(Long id) {
        return varianteProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarianteProducto", id));
    }

    @Override
    @Transactional
    public VarianteProducto create(VarianteProductoRequest request) {
        VarianteProducto variante = VarianteProducto.builder()
                .producto(productoService.findById(request.getProductoId()))
                .color(request.getColor())
                .talla(request.getTalla())
                .material(request.getMaterial())
                .peso(request.getPeso())
                .stock(request.getStock())
                .precio(request.getPrecio())
                .estacion(request.getEstacion())
                .build();
        return varianteProductoRepository.save(variante);
    }

    @Override
    @Transactional
    public VarianteProducto update(Long id, VarianteProductoRequest request) {
        VarianteProducto actual = findById(id);
        actual.setProducto(productoService.findById(request.getProductoId()));
        actual.setColor(request.getColor());
        actual.setTalla(request.getTalla());
        actual.setMaterial(request.getMaterial());
        actual.setPeso(request.getPeso());
        actual.setStock(request.getStock());
        actual.setPrecio(request.getPrecio());
        actual.setEstacion(request.getEstacion());
        return varianteProductoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (itemCarritoRepository.existsByVarianteId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar la variante id " + id + " porque está en carritos activos");
        }
        if (itemOrdenRepository.existsByVarianteId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar la variante id " + id + " porque tiene órdenes asociadas");
        }
        varianteProductoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPrecio(Long id) {
        return findById(id).getPrecio();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BusinessRuleException("La cantidad debe ser mayor a 0");
        }
        Integer stock = findById(id).getStock();
        return stock != null && stock >= cantidad;
    }

    @Override
    @Transactional
    public VarianteProducto descontarStock(Long id, Integer cantidad) {
        VarianteProducto variante = findById(id);
        int stockActual = variante.getStock() != null ? variante.getStock() : 0;
        if (stockActual < cantidad) {
            throw new BusinessRuleException("Stock insuficiente para variante id " + id
                    + ". Stock actual: " + stockActual + ", solicitado: " + cantidad);
        }
        variante.setStock(stockActual - cantidad);
        return varianteProductoRepository.save(variante);
    }

    @Override
    @Transactional
    public VarianteProducto restaurarStock(Long id, Integer cantidad) {
        VarianteProducto variante = findById(id);
        int stockActual = variante.getStock() != null ? variante.getStock() : 0;
        variante.setStock(stockActual + cantidad);
        return varianteProductoRepository.save(variante);
    }
}
