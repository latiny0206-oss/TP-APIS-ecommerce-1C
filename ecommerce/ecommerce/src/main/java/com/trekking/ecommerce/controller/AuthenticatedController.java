package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AuthenticatedController {

    @Autowired
    protected UsuarioService usuarioService;

    protected boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    protected Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByUsername(auth.getName());
    }

    protected void validarPropietario(Long propietarioId) {
        if (esAdmin()) return;
        if (!getUsuarioAutenticado().getId().equals(propietarioId)) {
            throw new AccessDeniedException("No tenés permiso para acceder a este recurso");
        }
    }
}
