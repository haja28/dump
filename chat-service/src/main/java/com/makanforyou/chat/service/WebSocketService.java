package com.makanforyou.chat.service;

import com.makanforyou.chat.dto.MessageDTO;
import com.makanforyou.chat.dto.NotificationDTO;
import com.makanforyou.chat.dto.TypingIndicatorDTO;
import com.makanforyou.chat.dto.WebSocketMessageDTO;
import com.makanforyou.chat.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for WebSocket real-time communication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String CONVERSATION_TOPIC = "/topic/conversations/";
    private static final String USER_QUEUE = "/queue/notifications";

    /**
     * Send a message to all subscribers of a conversation
     */
    public void sendMessageToConversation(Long conversationId, MessageDTO message) {
        log.debug("Sending message to conversation {}", conversationId);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(WebSocketMessageDTO.EventType.NEW_MESSAGE)
                .conversationId(conversationId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send delivery receipts to conversation
     */
    public void sendDeliveryReceipts(Long conversationId, Long userId) {
        log.debug("Sending delivery receipts for conversation {} from user {}", conversationId, userId);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(WebSocketMessageDTO.EventType.MESSAGE_DELIVERED)
                .conversationId(conversationId)
                .readReceipt(WebSocketMessageDTO.ReadReceiptDTO.builder()
                        .readerId(userId)
                        .readAt(LocalDateTime.now())
                        .status(Message.MessageStatus.DELIVERED)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send read receipts to conversation
     */
    public void sendReadReceipts(Long conversationId, Long userId) {
        log.debug("Sending read receipts for conversation {} from user {}", conversationId, userId);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(WebSocketMessageDTO.EventType.MESSAGE_READ)
                .conversationId(conversationId)
                .readReceipt(WebSocketMessageDTO.ReadReceiptDTO.builder()
                        .readerId(userId)
                        .readAt(LocalDateTime.now())
                        .status(Message.MessageStatus.READ)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send read receipt for a single message
     */
    public void sendReadReceipt(Long conversationId, Long messageId, Long userId) {
        log.debug("Sending read receipt for message {} in conversation {}", messageId, conversationId);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(WebSocketMessageDTO.EventType.MESSAGE_READ)
                .conversationId(conversationId)
                .readReceipt(WebSocketMessageDTO.ReadReceiptDTO.builder()
                        .messageId(messageId)
                        .readerId(userId)
                        .readAt(LocalDateTime.now())
                        .status(Message.MessageStatus.READ)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send typing indicator to conversation
     */
    public void sendTypingIndicator(Long conversationId, Long userId, String userName, boolean isTyping) {
        log.debug("Sending typing indicator for conversation {} from user {}: {}", conversationId, userId, isTyping);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(isTyping ? WebSocketMessageDTO.EventType.TYPING_START : WebSocketMessageDTO.EventType.TYPING_STOP)
                .conversationId(conversationId)
                .typingIndicator(TypingIndicatorDTO.builder()
                        .conversationId(conversationId)
                        .userId(userId)
                        .userName(userName)
                        .isTyping(isTyping)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send notification to a specific user
     */
    public void sendNotificationToUser(Long userId, Message.SenderType userType, NotificationDTO notification) {
        log.debug("Sending notification to {} {}", userType, userId);

        String userDestination = "/user/" + userType.name().toLowerCase() + "/" + userId + USER_QUEUE;
        messagingTemplate.convertAndSend(userDestination, notification);
    }

    /**
     * Send online status update
     */
    public void sendOnlineStatus(Long conversationId, Long userId, boolean isOnline) {
        log.debug("Sending online status for user {} in conversation {}: {}", userId, conversationId, isOnline);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(isOnline ? WebSocketMessageDTO.EventType.USER_ONLINE : WebSocketMessageDTO.EventType.USER_OFFLINE)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }

    /**
     * Send conversation updated event
     */
    public void sendConversationUpdated(Long conversationId) {
        log.debug("Sending conversation updated event for {}", conversationId);

        WebSocketMessageDTO wsMessage = WebSocketMessageDTO.builder()
                .eventType(WebSocketMessageDTO.EventType.CONVERSATION_UPDATED)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(CONVERSATION_TOPIC + conversationId, wsMessage);
    }
}
