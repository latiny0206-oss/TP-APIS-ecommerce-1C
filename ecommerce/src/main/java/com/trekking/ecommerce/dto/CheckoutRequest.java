package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

    @Size(max = 100)
    private String nombreDestinatario;

    @Size(max = 250)
    private String direccion;

    @Size(max = 100)
    private String ciudad;

    @Size(max = 100)
    private String provincia;

    @Size(max = 20)
    private String codigoPostal;

    @Size(max = 30)
    private String telefono;

    @Size(max = 50)
    private String metodoPago;
}
