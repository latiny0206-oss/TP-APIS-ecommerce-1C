package com.trekking.ecommerce.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado/a con id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}