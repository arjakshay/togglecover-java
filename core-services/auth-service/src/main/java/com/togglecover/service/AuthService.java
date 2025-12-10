package com.togglecover.service;

import com.togglecover.model.dto.*;
import com.togglecover.model.entity.RefreshToken;
import com.togglecover.repository.UserRepository;
import com.togglecover.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User already exists with username: " + request.getUsername());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate tokens
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = (User) authentication.getPrincipal();
        log.info("User logged in successfully: {}", user.getEmail());

        // Generate tokens
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if token is expired
        refreshTokenService.verifyExpiration(storedToken);

        // Get user
        User user = storedToken.getUser();

        // Generate new tokens
        return generateAuthResponse(user);
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(token);

            if (!isValid) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid token")
                        .build();
            }

            String email = jwtTokenProvider.extractUsername(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return TokenValidationResponse.builder()
                    .valid(true)
                    .message("Token is valid")
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .userId(user.getId())
                    .issuedAt(LocalDateTime.now().minusSeconds(jwtTokenProvider.getExpiration() / 1000))
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpiration() / 1000))
                    .build();

        } catch (Exception e) {
            log.error("Token validation error: ", e);
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Extract email from access token
        String email = jwtTokenProvider.extractUsername(accessToken);

        if (email != null) {
            // Invalidate refresh token if provided
            if (refreshToken != null && !refreshToken.isEmpty()) {
                refreshTokenService.deleteByToken(refreshToken);
            }

            log.info("User logged out: {}", email);
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        // Create claims for JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        // Generate access token using common JwtTokenProvider
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), "USER", claims);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration() / 1000) // Convert to seconds
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }
}