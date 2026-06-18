package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import com.trekking.ecommerce.dto.UsuarioUpdateRequest;
import com.trekking.ecommerce.model.Usuario;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> findAll();
    UsuarioResponse findById(Long id);
    UsuarioResponse create(UsuarioRequest request);
    UsuarioResponse update(Long id, UsuarioUpdateRequest request);
    void delete(Long id);
    Usuario findByUsername(String username);
    Usuario findEntityById(Long id);
    Usuario findByEmail(String email);
    void createPasswordResetTokenForUser(Usuario user, String token);
    void resetPassword(String token, String newPassword);
}

