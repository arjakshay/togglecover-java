package com.togglecover.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "USER-SERVICE", path = "/api/users")
public interface UserServiceClient {

    @PostMapping("/validate-credentials")
    UserValidationResponse validateCredentials(@RequestBody UserValidationRequest request);

    @PostMapping("/validate-token")
    UserValidationResponse validateUserFromToken(@RequestBody UserValidationRequest request);
}

// User validation DTOs


