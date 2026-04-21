package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.MarcaRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.repository.MarcaRepository;
import com.trekking.ecommerce.service.MarcaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Marca> findAll() {
        return marcaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Marca findById(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca", id));
    }

    @Override
    @Transactional
    public Marca create(MarcaRequest request) {
        Marca marca = Marca.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return marcaRepository.save(marca);
    }

    @Override
    @Transactional
    public Marca update(Long id, MarcaRequest request) {
        Marca actual = findById(id);
        actual.setNombre(request.getNombre());
        actual.setDescripcion(request.getDescripcion());
        return marcaRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        marcaRepository.deleteById(id);
    }
}
