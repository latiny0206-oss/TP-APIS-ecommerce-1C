package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.ProductoRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.CategoriaService;
import com.trekking.ecommerce.service.MarcaService;
import com.trekking.ecommerce.service.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByCategoria(Long categoriaId) {
        categoriaService.findById(categoriaId);
        return productoRepository.findByCategoriaId(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByMarca(Long marcaId) {
        marcaService.findById(marcaId);
        return productoRepository.findByMarcaId(marcaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByEstado(EstadoProducto estado) {
        return productoRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    @Override
    @Transactional
    public Producto create(ProductoRequest request) {
        Producto producto = Producto.builder()
                .marca(marcaService.findById(request.getMarcaId()))
                .categoria(categoriaService.findById(request.getCategoriaId()))
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .precioBase(request.getPrecioBase())
                .build();
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto update(Long id, ProductoRequest request) {
        Producto actual = findById(id);
        actual.setMarca(marcaService.findById(request.getMarcaId()));
        actual.setCategoria(categoriaService.findById(request.getCategoriaId()));
        actual.setNombre(request.getNombre());
        actual.setDescripcion(request.getDescripcion());
        actual.setEstado(request.getEstado());
        actual.setPrecioBase(request.getPrecioBase());
        return productoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(Long id) {
        Producto producto = findById(id);
        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            return false;
        }
        return varianteProductoRepository.findByProductoId(id).stream()
                .anyMatch(v -> v.getStock() != null && v.getStock() > 0);
    }
}
