package com.togglecover.userservice.model.dto;

import lombok.Data;

@Data
public class UserValidationResponse {
    private Boolean valid;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;

    public UserValidationResponse(Boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public UserValidationResponse(Boolean valid, Long userId, String username,
                                  String email, String role, String message) {
        this.valid = valid;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
    }
}