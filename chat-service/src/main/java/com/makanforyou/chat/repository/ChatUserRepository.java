package com.makanforyou.chat.repository;

import com.makanforyou.chat.entity.ChatUser;
import com.makanforyou.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for ChatUser entity
 */
@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {

    /**
     * Find chat user by user ID and type
     */
    Optional<ChatUser> findByUserIdAndUserType(Long userId, Message.SenderType userType);

    /**
     * Update online status
     */
    @Modifying
    @Query("UPDATE ChatUser u SET u.isOnline = :isOnline, u.lastSeen = :lastSeen WHERE u.userId = :userId AND u.userType = :userType")
    void updateOnlineStatus(@Param("userId") Long userId, @Param("userType") Message.SenderType userType,
                           @Param("isOnline") Boolean isOnline, @Param("lastSeen") LocalDateTime lastSeen);

    /**
     * Update push token
     */
    @Modifying
    @Query("UPDATE ChatUser u SET u.pushToken = :pushToken WHERE u.userId = :userId AND u.userType = :userType")
    void updatePushToken(@Param("userId") Long userId, @Param("userType") Message.SenderType userType, @Param("pushToken") String pushToken);

    /**
     * Check if user is online
     */
    @Query("SELECT u.isOnline FROM ChatUser u WHERE u.userId = :userId AND u.userType = :userType")
    Boolean isUserOnline(@Param("userId") Long userId, @Param("userType") Message.SenderType userType);

    /**
     * Get last seen timestamp
     */
    @Query("SELECT u.lastSeen FROM ChatUser u WHERE u.userId = :userId AND u.userType = :userType")
    LocalDateTime getLastSeen(@Param("userId") Long userId, @Param("userType") Message.SenderType userType);
}
