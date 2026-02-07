package com.makanforyou.auth.controller;

import com.makanforyou.auth.dto.*;
import com.makanforyou.auth.service.AuthService;
import com.makanforyou.auth.service.VerificationService;
import com.makanforyou.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Login user
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Refresh access token
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Get current user info
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve current authenticated user details")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        log.info("Getting user info for ID: {}", userId);
        UserDTO user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Get user by ID
     * GET /api/v1/auth/users/{userId}
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        log.info("Getting user info for ID: {}", userId);
        UserDTO user = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Verify email via GET (for email link click)
     * GET /api/v1/auth/verify-email?token=xxx
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email via link", description = "Verify user email using token from URL")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyEmailGet(@RequestParam("token") String token) {
        log.info("Email verification request received via GET");
        return verifyEmailInternal(token);
    }

    /**
     * Verify email via POST
     * POST /api/v1/auth/verify-email
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email using token")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification request received via POST");
        return verifyEmailInternal(request.getToken());
    }

    private ResponseEntity<ApiResponse<VerificationResponse>> verifyEmailInternal(String token) {
        var user = verificationService.verifyEmail(token);
        VerificationResponse response = VerificationResponse.builder()
                .message("Email verified successfully")
                .verified(true)
                .user(UserDTO.builder()
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
                        .build())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Email verified successfully"));
    }

    /**
     * Resend verification email
     * POST /api/v1/auth/resend-verification-email
     */
    @PostMapping("/resend-verification-email")
    @Operation(summary = "Resend verification email", description = "Resend verification email to user")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        log.info("Resend verification email request received for: {}", request.getEmail());
        verificationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification email sent successfully"));
    }
}
