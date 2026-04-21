package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.CategoriaRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Categoria;
import com.trekking.ecommerce.repository.CategoriaRepository;
import com.trekking.ecommerce.service.CategoriaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria findById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    @Override
    @Transactional
    public Categoria create(CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria update(Long id, CategoriaRequest request) {
        Categoria actual = findById(id);
        actual.setNombre(request.getNombre());
        actual.setDescripcion(request.getDescripcion());
        return categoriaRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        categoriaRepository.deleteById(id);
    }
}
