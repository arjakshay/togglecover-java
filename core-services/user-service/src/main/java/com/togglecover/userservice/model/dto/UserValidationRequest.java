package com.togglecover.userservice.model.dto;

import lombok.Data;

@Data
public class UserValidationRequest {
    private String email;
    private String password;
    private String token;
}