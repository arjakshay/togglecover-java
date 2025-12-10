package com.togglecover.model.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String username;
    private String role;
    private Long userId;

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn,
                        String username, String role, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.role = role;
        this.userId = userId;
    }
}
