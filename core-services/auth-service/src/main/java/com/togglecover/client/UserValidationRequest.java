package com.togglecover.client;

import lombok.Data;

@Data
public class UserValidationRequest {
    private String email;
    private String password;
    private String token;
}