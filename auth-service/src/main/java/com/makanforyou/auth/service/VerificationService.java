package com.makanforyou.auth.service;

import com.makanforyou.auth.entity.EmailVerificationToken;
import com.makanforyou.auth.entity.User;
import com.makanforyou.auth.repository.EmailVerificationTokenRepository;
import com.makanforyou.auth.repository.UserRepository;
import com.makanforyou.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for email verification operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.email.verification-token-expiration-hours:24}")
    private Integer tokenExpirationHours;

    /**
     * Generate and send verification email
     */
    public void generateAndSendVerificationEmail(User user) {
        try {
            // Delete any existing unused tokens
            tokenRepository.deleteByUserAndIsUsedFalse(user);

            // Generate new token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(expiresAt)
                    .isUsed(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            tokenRepository.save(verificationToken);

            // Send email
            String fullName = user.getFirstName() + " " + user.getLastName();
            emailService.sendVerificationEmail(user.getEmail(), fullName, token);

            log.info("Verification email generated and sent for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error generating verification email for user {}: {}", user.getEmail(), e.getMessage());
            throw new ApplicationException("EMAIL_SEND_ERROR", "Failed to send verification email");
        }
    }

    /**
     * Verify email using token
     */
    public User verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ApplicationException("INVALID_TOKEN", "Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new ApplicationException("TOKEN_EXPIRED", "Verification token has expired");
        }

        if (verificationToken.getIsUsed()) {
            throw new ApplicationException("TOKEN_ALREADY_USED", "Verification token has already been used");
        }

        // Mark token as used
        verificationToken.setIsUsed(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        // Update user
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Send welcome email
        String fullName = user.getFirstName() + " " + user.getLastName();
        emailService.sendWelcomeEmail(user.getEmail(), fullName);

        log.info("Email verified successfully for user: {}", user.getEmail());
        return user;
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException("USER_NOT_FOUND", "User not found"));

        if (user.getEmailVerified()) {
            throw new ApplicationException("EMAIL_ALREADY_VERIFIED", "Email is already verified");
        }

        generateAndSendVerificationEmail(user);
        log.info("Verification email resent for user: {}", email);
    }

    /**
     * Clean up expired tokens (can be run periodically)
     */
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired verification tokens");
    }
}
