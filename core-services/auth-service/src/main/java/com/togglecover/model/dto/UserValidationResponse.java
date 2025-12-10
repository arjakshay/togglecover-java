package com.togglecover.model.dto;

import lombok.Data;

@Data
public class UserValidationResponse {
    private Boolean valid;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;
}