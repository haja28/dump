package com.makanforyou.auth.repository;

import com.makanforyou.auth.entity.EmailVerificationToken;
import com.makanforyou.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for EmailVerificationToken entity
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUserAndIsUsedFalse(User user);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    void deleteByUserAndIsUsedFalse(User user);
}
