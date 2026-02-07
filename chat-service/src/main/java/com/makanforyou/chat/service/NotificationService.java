package com.makanforyou.chat.service;

import com.makanforyou.chat.dto.NotificationDTO;
import com.makanforyou.chat.entity.ChatNotification;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.mapper.ChatMapper;
import com.makanforyou.chat.repository.ChatNotificationRepository;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Service for managing chat notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ChatNotificationRepository notificationRepository;
    private final ChatMapper chatMapper;
    @Lazy
    private final WebSocketService webSocketService;

    /**
     * Create notification for new message
     */
    @Transactional
    public NotificationDTO createNewMessageNotification(Long userId, Message.SenderType userType,
                                                         Long conversationId, Long messageId, String messageContent) {
        log.info("Creating new message notification for {} {} in conversation {}", userType, userId, conversationId);

        String preview = chatMapper.truncateForPreview(messageContent, 50);

        ChatNotification notification = ChatNotification.builder()
                .userId(userId)
                .userType(userType)
                .conversationId(conversationId)
                .messageId(messageId)
                .type(ChatNotification.NotificationType.NEW_MESSAGE)
                .title("New Message")
                .body(preview)
                .build();

        notification = notificationRepository.save(notification);
        NotificationDTO dto = chatMapper.toNotificationDTO(notification);

        // Send via WebSocket
        try {
            webSocketService.sendNotificationToUser(userId, userType, dto);
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket", e);
        }

        return dto;
    }

    /**
     * Create notification for new conversation
     */
    @Transactional
    public NotificationDTO createNewConversationNotification(Long userId, Message.SenderType userType,
                                                              Long conversationId, String title) {
        log.info("Creating new conversation notification for {} {}", userType, userId);

        ChatNotification notification = ChatNotification.builder()
                .userId(userId)
                .userType(userType)
                .conversationId(conversationId)
                .type(ChatNotification.NotificationType.NEW_CONVERSATION)
                .title("New Chat Request")
                .body(title != null ? title : "A customer wants to chat with you")
                .build();

        notification = notificationRepository.save(notification);
        NotificationDTO dto = chatMapper.toNotificationDTO(notification);

        // Send via WebSocket
        try {
            webSocketService.sendNotificationToUser(userId, userType, dto);
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket", e);
        }

        return dto;
    }

    /**
     * Create notification for order update
     */
    @Transactional
    public NotificationDTO createOrderUpdateNotification(Long userId, Message.SenderType userType,
                                                          Long conversationId, Long messageId, String updateMessage) {
        log.info("Creating order update notification for {} {}", userType, userId);

        ChatNotification notification = ChatNotification.builder()
                .userId(userId)
                .userType(userType)
                .conversationId(conversationId)
                .messageId(messageId)
                .type(ChatNotification.NotificationType.ORDER_UPDATE)
                .title("Order Update")
                .body(updateMessage)
                .build();

        notification = notificationRepository.save(notification);
        NotificationDTO dto = chatMapper.toNotificationDTO(notification);

        // Send via WebSocket
        try {
            webSocketService.sendNotificationToUser(userId, userType, dto);
        } catch (Exception e) {
            log.error("Failed to send notification via WebSocket", e);
        }

        return dto;
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getNotifications(Long userId, Message.SenderType userType, Pageable pageable) {
        Page<ChatNotification> page = notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(userId, userType, pageable);
        return toPagedResponse(page);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getUnreadNotifications(Long userId, Message.SenderType userType, Pageable pageable) {
        Page<ChatNotification> page = notificationRepository.findByUserIdAndUserTypeAndIsReadFalseOrderByCreatedAtDesc(userId, userType, pageable);
        return toPagedResponse(page);
    }

    /**
     * Count unread notifications
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId, Message.SenderType userType) {
        return notificationRepository.countByUserIdAndUserTypeAndIsReadFalse(userId, userType);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId, Message.SenderType userType) {
        log.info("Marking all notifications as read for {} {}", userType, userId);
        notificationRepository.markAllAsRead(userId, userType, LocalDateTime.now());
    }

    /**
     * Mark conversation notifications as read
     */
    @Transactional
    public void markConversationNotificationsAsRead(Long conversationId, Long userId) {
        log.info("Marking conversation {} notifications as read for user {}", conversationId, userId);
        notificationRepository.markConversationNotificationsAsRead(conversationId, userId, LocalDateTime.now());
    }

    /**
     * Delete all notifications for a user
     */
    @Transactional
    public void clearAllNotifications(Long userId, Message.SenderType userType) {
        log.info("Clearing all notifications for {} {}", userType, userId);
        notificationRepository.deleteByUserIdAndUserType(userId, userType);
    }

    /**
     * Cleanup old read notifications (can be scheduled)
     */
    @Transactional
    public void cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        log.info("Cleaning up read notifications older than {}", cutoff);
        notificationRepository.deleteOldReadNotifications(cutoff);
    }

    /**
     * Convert page to paged response
     */
    private PagedResponse<NotificationDTO> toPagedResponse(Page<ChatNotification> page) {
        return PagedResponse.<NotificationDTO>builder()
                .content(page.getContent().stream()
                        .map(chatMapper::toNotificationDTO)
                        .collect(Collectors.toList()))
                .pagination(PaginationMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}
