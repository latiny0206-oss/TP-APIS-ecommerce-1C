package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[A-Z].*", message = "La contrasena debe contener al menos una letra mayuscula")
    @Pattern(regexp = ".*\\d.*", message = "La contrasena debe contener al menos un numero")
    private String password;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;
}
