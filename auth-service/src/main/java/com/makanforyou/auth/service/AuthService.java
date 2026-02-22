package com.makanforyou.auth.service;

import com.makanforyou.auth.dto.*;
import com.makanforyou.auth.entity.User;
import com.makanforyou.auth.repository.UserRepository;
import com.makanforyou.auth.security.JwtTokenProvider;
import com.makanforyou.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException("USER_ALREADY_EXISTS", "Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ApplicationException("PHONE_ALREADY_EXISTS", "Phone number already registered");
        }

        // Create new user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.UserRole.CUSTOMER)
                .isActive(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        // Send verification email
        verificationService.generateAndSendVerificationEmail(user);

        return generateAuthResponse(user);
    }

    /**
     * Login user and return tokens
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "Invalid email or password"));

        if (!user.getIsActive()) {
            throw new ApplicationException("USER_INACTIVE", "User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApplicationException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new ApplicationException("INVALID_TOKEN", "Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.extractUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found"));

        return generateAuthResponse(user);
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found"));
        return mapToDTO(user);
    }

    /**
     * Get current user info
     */
    public UserDTO getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    /**
     * Generate authentication response with tokens
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime())
                .user(mapToDTO(user))
                .build();
    }

    /**
     * Map User entity to DTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
