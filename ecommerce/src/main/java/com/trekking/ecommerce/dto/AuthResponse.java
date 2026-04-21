package com.trekking.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class AuthResponse {
    String token;
    String username;
    String rol;
}
