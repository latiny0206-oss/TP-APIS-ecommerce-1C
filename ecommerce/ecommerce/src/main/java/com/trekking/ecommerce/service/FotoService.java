package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Foto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FotoService {
    List<Foto> findAll();
    List<Foto> findByVariante(Long varianteId);
    Foto findById(Long id);
    Foto create(Long varianteId, Integer orden, MultipartFile archivo);
    Foto update(Long id, Long varianteId, Integer orden, MultipartFile archivo);
    void delete(Long id);
}
