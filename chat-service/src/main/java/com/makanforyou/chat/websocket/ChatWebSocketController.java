package com.makanforyou.chat.websocket;

import com.makanforyou.chat.dto.MessageDTO;
import com.makanforyou.chat.dto.SendMessageRequest;
import com.makanforyou.chat.dto.TypingIndicatorDTO;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.service.MessageService;
import com.makanforyou.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time chat messaging
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final WebSocketService webSocketService;

    /**
     * Handle sending a message via WebSocket
     * Client sends to: /app/chat/{conversationId}/send
     */
    @MessageMapping("/chat/{conversationId}/send")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Header("userId") Long userId,
            @Header("userType") String userType,
            @Payload SendMessageRequest request) {
        log.info("WebSocket: Sending message to conversation {} from {} {}", conversationId, userType, userId);

        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;

        MessageDTO message = messageService.sendMessage(conversationId, userId, senderType, request);

        // Message is automatically broadcast via WebSocketService in MessageService
        log.debug("WebSocket: Message {} sent and broadcast", message.getId());
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat/{conversationId}/typing
     */
    @MessageMapping("/chat/{conversationId}/typing")
    public void typingIndicator(
            @DestinationVariable Long conversationId,
            @Payload TypingIndicatorDTO typingIndicator) {
        log.debug("WebSocket: Typing indicator for conversation {} from user {}: {}",
                conversationId, typingIndicator.getUserId(), typingIndicator.getIsTyping());

        webSocketService.sendTypingIndicator(
                conversationId,
                typingIndicator.getUserId(),
                typingIndicator.getUserName(),
                typingIndicator.getIsTyping()
        );
    }

    /**
     * Handle marking messages as read via WebSocket
     * Client sends to: /app/chat/{conversationId}/read
     */
    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(
            @DestinationVariable Long conversationId,
            @Header("userId") Long userId,
            @Header("userType") String userType) {
        log.info("WebSocket: Marking messages as read in conversation {} for user {}", conversationId, userId);

        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        messageService.markAsRead(conversationId, userId, isCustomer);
    }

    /**
     * Handle marking messages as delivered via WebSocket
     * Client sends to: /app/chat/{conversationId}/delivered
     */
    @MessageMapping("/chat/{conversationId}/delivered")
    public void markAsDelivered(
            @DestinationVariable Long conversationId,
            @Header("userId") Long userId) {
        log.info("WebSocket: Marking messages as delivered in conversation {} for user {}", conversationId, userId);

        messageService.markAsDelivered(conversationId, userId);
    }

    /**
     * Handle user coming online
     * Client sends to: /app/user/online
     */
    @MessageMapping("/user/online")
    public void userOnline(@Header("userId") Long userId, @Header("userType") String userType) {
        log.info("WebSocket: User {} {} came online", userType, userId);
        // TODO: Update user online status in database and notify relevant conversations
    }

    /**
     * Handle user going offline
     * Client sends to: /app/user/offline
     */
    @MessageMapping("/user/offline")
    public void userOffline(@Header("userId") Long userId, @Header("userType") String userType) {
        log.info("WebSocket: User {} {} went offline", userType, userId);
        // TODO: Update user online status in database and notify relevant conversations
    }

    /**
     * Handle connection error - returns to user queue
     */
    @MessageMapping("/error")
    @SendToUser("/queue/errors")
    public String handleError(Exception exception) {
        log.error("WebSocket error: {}", exception.getMessage());
        return "Error: " + exception.getMessage();
    }
}
