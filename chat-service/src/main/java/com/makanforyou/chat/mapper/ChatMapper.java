package com.makanforyou.chat.mapper;

import com.makanforyou.chat.dto.ConversationDTO;
import com.makanforyou.chat.dto.MessageDTO;
import com.makanforyou.chat.dto.NotificationDTO;
import com.makanforyou.chat.entity.ChatNotification;
import com.makanforyou.chat.entity.Conversation;
import com.makanforyou.chat.entity.Message;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting entities to DTOs
 */
@Component
public class ChatMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a");
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    /**
     * Convert Conversation entity to DTO
     */
    public ConversationDTO toConversationDTO(Conversation conversation, Long requesterId, boolean isCustomer) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .orderId(conversation.getOrderId())
                .customerId(conversation.getCustomerId())
                .kitchenId(conversation.getKitchenId())
                .kitchenUserId(conversation.getKitchenUserId())
                .title(conversation.getTitle())
                .status(conversation.getStatus())
                .unreadCount(isCustomer ? conversation.getCustomerUnreadCount() : conversation.getKitchenUnreadCount())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    /**
     * Convert Message entity to DTO
     * @deprecated Use toMessageDTO(Message, Long, Message.SenderType) instead for accurate isOwnMessage
     */
    public MessageDTO toMessageDTO(Message message, Long requesterId) {
        boolean isOwnMessage = message.getSenderId().equals(requesterId);

        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentType(message.getAttachmentType())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .editedAt(message.getEditedAt())
                .isEdited(message.getEditedAt() != null)
                .isOwnMessage(isOwnMessage)
                .formattedTime(formatRelativeTime(message.getSentAt()))
                .build();
    }

    /**
     * Convert Message entity to DTO with proper isOwnMessage calculation
     * considering both userId and userType
     */
    public MessageDTO toMessageDTO(Message message, Long requesterId, Message.SenderType requesterType) {
        // A message is "own" if:
        // 1. The sender type matches the requester type, AND
        // 2. The sender ID matches the requester ID
        boolean isOwnMessage = message.getSenderType() == requesterType &&
                               message.getSenderId().equals(requesterId);

        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentType(message.getAttachmentType())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .editedAt(message.getEditedAt())
                .isEdited(message.getEditedAt() != null)
                .isOwnMessage(isOwnMessage)
                .formattedTime(formatRelativeTime(message.getSentAt()))
                .build();
    }

    /**
     * Convert Message entity to DTO for WebSocket broadcast.
     * Does not include isOwnMessage so each client can determine it based on sender_type.
     */
    public MessageDTO toMessageDTOForBroadcast(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentType(message.getAttachmentType())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .editedAt(message.getEditedAt())
                .isEdited(message.getEditedAt() != null)
                .isOwnMessage(null)  // Let client determine based on sender_type
                .formattedTime(formatRelativeTime(message.getSentAt()))
                .build();
    }

    /**
     * Convert ChatNotification entity to DTO
     */
    public NotificationDTO toNotificationDTO(ChatNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .conversationId(notification.getConversationId())
                .messageId(notification.getMessageId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .formattedTime(formatRelativeTime(notification.getCreatedAt()))
                .build();
    }

    /**
     * Format timestamp to relative time string
     * "Just now" (< 1 minute)
     * "5 minutes ago"
     * "Today at 2:30 PM"
     * "Yesterday at 10:15 AM"
     * "Jan 15 at 3:45 PM" (older messages)
     */
    public String formatRelativeTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(timestamp, now);

        // Less than 1 minute
        if (duration.toMinutes() < 1) {
            return "Just now";
        }

        // Less than 1 hour
        if (duration.toHours() < 1) {
            long minutes = duration.toMinutes();
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        // Today
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return "Today at " + timestamp.format(TIME_FORMATTER);
        }

        // Yesterday
        if (timestamp.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday at " + timestamp.format(TIME_FORMATTER);
        }

        // Same year
        if (timestamp.getYear() == now.getYear()) {
            return timestamp.format(DATE_TIME_FORMATTER);
        }

        // Different year
        return timestamp.format(FULL_DATE_FORMATTER);
    }

    /**
     * Truncate message for preview
     */
    public String truncateForPreview(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength - 3) + "...";
    }
}
