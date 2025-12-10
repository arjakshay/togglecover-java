package com.togglecover.userservice.service;

import com.togglecover.common.models.UserDTO;
import com.togglecover.userservice.model.User;
import com.togglecover.userservice.model.dto.UserRegistrationRequest;
import com.togglecover.userservice.model.dto.UserValidationResponse;
import com.togglecover.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole("USER"); // Default role

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return convertToDTO(savedUser);
    }

    public UserValidationResponse validateCredentials(String email, String password) {
        log.debug("Validating credentials for email: {}", email);

        return userRepository.findByEmail(email)
                .filter(user -> {
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        log.warn("Password mismatch for user: {}", email);
                    }
                    return matches;
                })
                .map(user -> new UserValidationResponse(
                        true,
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        "Credentials are valid"
                ))
                .orElse(new UserValidationResponse(
                        false,
                        null,
                        null,
                        null,
                        null,
                        "Invalid email or password"
                ));
    }

    public UserValidationResponse validateUserFromToken(String token) {
        log.debug("Validating user from token");

        // In a real implementation, you would decode the token and validate it
        // For now, we'll extract user ID from token (assuming token format)
        try {
            // Parse token to extract user info
            // This is a simplified example - in production, use JWT library
            if (token.startsWith("user_")) {
                String userIdStr = token.substring(5);
                Long userId = Long.parseLong(userIdStr);

                return userRepository.findById(userId)
                        .map(user -> new UserValidationResponse(
                                true,
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole(),
                                "User validated from token"
                        ))
                        .orElse(new UserValidationResponse(
                                false,
                                null,
                                null,
                                null,
                                null,
                                "User not found"
                        ));
            }
        } catch (Exception e) {
            log.error("Error validating user from token: {}", e.getMessage());
        }

        return new UserValidationResponse(
                false,
                null,
                null,
                null,
                null,
                "Invalid token format"
        );
    }

    public UserDTO getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return convertToDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserRegistrationRequest request) {
        log.info("Updating user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update fields
        user.setName(request.getName());

        // Only update email if it's not already taken by another user
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already taken: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}