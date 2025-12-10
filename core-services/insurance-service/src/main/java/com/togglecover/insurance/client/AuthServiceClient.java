package com.togglecover.insurance.client;


import com.togglecover.insurance.model.dto.TokenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AUTH-SERVICE", path = "/auth/api/auth")
public interface AuthServiceClient {

    @PostMapping("/validate")
    TokenValidationResponse validateToken(@RequestHeader("Authorization") String authHeader);
}