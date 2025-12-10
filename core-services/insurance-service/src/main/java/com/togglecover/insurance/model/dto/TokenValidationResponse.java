package com.togglecover.insurance.model.dto;

import lombok.Data;

@Data
public class TokenValidationResponse {
    private Boolean valid;
    private String username;
    private String role;
    private Long userId;
    private String message;
}
