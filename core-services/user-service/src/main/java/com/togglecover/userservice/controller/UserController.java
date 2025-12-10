package com.togglecover.userservice.controller;


import com.togglecover.common.models.UserDTO;
import com.togglecover.userservice.model.dto.UserRegistrationRequest;
import com.togglecover.userservice.model.dto.UserValidationRequest;
import com.togglecover.userservice.model.dto.UserValidationResponse;
import com.togglecover.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserDTO user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/validate-credentials")
    @Operation(summary = "Validate user credentials (used by Auth Service)")
    public ResponseEntity<UserValidationResponse> validateCredentials(
            @Valid @RequestBody UserValidationRequest request) {
        UserValidationResponse response = userService.validateCredentials(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate user from token (used by Auth Service)")
    public ResponseEntity<UserValidationResponse> validateUserFromToken(
            @Valid @RequestBody UserValidationRequest request) {
        UserValidationResponse response = userService.validateUserFromToken(request.getToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRegistrationRequest request) {
        UserDTO user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }
}