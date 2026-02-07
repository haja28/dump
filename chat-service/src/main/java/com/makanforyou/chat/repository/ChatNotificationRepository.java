package com.makanforyou.chat.repository;

import com.makanforyou.chat.entity.ChatNotification;
import com.makanforyou.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatNotification entity
 */
@Repository
public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {

    /**
     * Find all notifications for a user
     */
    Page<ChatNotification> findByUserIdAndUserTypeOrderByCreatedAtDesc(
            Long userId, Message.SenderType userType, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<ChatNotification> findByUserIdAndUserTypeAndIsReadFalseOrderByCreatedAtDesc(
            Long userId, Message.SenderType userType, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    Long countByUserIdAndUserTypeAndIsReadFalse(Long userId, Message.SenderType userType);

    /**
     * Find notifications for a conversation
     */
    List<ChatNotification> findByConversationIdAndUserIdOrderByCreatedAtDesc(Long conversationId, Long userId);

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE ChatNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE ChatNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId, @Param("userType") Message.SenderType userType, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all notifications as read for a conversation
     */
    @Modifying
    @Query("UPDATE ChatNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.conversationId = :conversationId AND n.userId = :userId AND n.isRead = false")
    void markConversationNotificationsAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete all notifications for a user
     */
    @Modifying
    void deleteByUserIdAndUserType(Long userId, Message.SenderType userType);

    /**
     * Delete old read notifications
     */
    @Modifying
    @Query("DELETE FROM ChatNotification n WHERE n.isRead = true AND n.readAt < :before")
    void deleteOldReadNotifications(@Param("before") LocalDateTime before);
}
