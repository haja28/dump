package com.makanforyou.chat.controller;

import com.makanforyou.chat.dto.MessageDTO;
import com.makanforyou.chat.dto.SendMessageRequest;
import com.makanforyou.chat.entity.Conversation;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.service.ConversationService;
import com.makanforyou.chat.service.MessageService;
import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for message management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat message management endpoints")
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    /**
     * Send a new message
     * POST /api/v1/conversations/{conversationId}/messages
     */
    @PostMapping
    @Operation(summary = "Send message", description = "Send a new message in a conversation")
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @PathVariable Long conversationId,
            @Parameter(description = "Sender ID from auth header")
            @RequestHeader("X-User-Id") Long senderId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to conversation {} from {} {}", conversationId, userType, senderId);

        // Validate access
        Conversation conversation = conversationService.findConversationById(conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        conversationService.validateAccess(conversation, senderId, isCustomer);

        Message.SenderType senderType = isCustomer ? Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        MessageDTO message = messageService.sendMessage(conversationId, senderId, senderType, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, "Message sent successfully"));
    }

    /**
     * Get messages in conversation with pagination
     * GET /api/v1/conversations/{conversationId}/messages
     */
    @GetMapping
    @Operation(summary = "Get messages", description = "Get messages in a conversation")
    public ResponseEntity<ApiResponse<PagedResponse<MessageDTO>>> getMessages(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        log.info("Fetching messages for conversation {} by {} {}", conversationId, userType, userId);

        // Validate access
        Conversation conversation = conversationService.findConversationById(conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        conversationService.validateAccess(conversation, userId, isCustomer);

        // Convert userType string to enum
        Message.SenderType requesterType = isCustomer ? Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MessageDTO> response = messageService.getMessages(conversationId, userId, requesterType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all messages in conversation (for initial load)
     * GET /api/v1/conversations/{conversationId}/messages/all
     */
    @GetMapping("/all")
    @Operation(summary = "Get all messages", description = "Get all messages in a conversation")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getAllMessages(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Fetching all messages for conversation {} by {} {}", conversationId, userType, userId);

        // Validate access
        Conversation conversation = conversationService.findConversationById(conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        conversationService.validateAccess(conversation, userId, isCustomer);

        // Convert userType string to enum
        Message.SenderType requesterType = isCustomer ? Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;

        List<MessageDTO> messages = messageService.getAllMessages(conversationId, userId, requesterType);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Get messages after a timestamp (for sync)
     * GET /api/v1/conversations/{conversationId}/messages/after
     */
    @GetMapping("/after")
    @Operation(summary = "Get new messages", description = "Get messages after a specific timestamp")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessagesAfter(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {
        log.info("Fetching messages after {} for conversation {}", after, conversationId);

        // Validate access
        Conversation conversation = conversationService.findConversationById(conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        conversationService.validateAccess(conversation, userId, isCustomer);

        List<MessageDTO> messages = messageService.getMessagesAfter(conversationId, userId, after);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Mark messages as read
     * PUT /api/v1/conversations/{conversationId}/messages/read
     */
    @PutMapping("/read")
    @Operation(summary = "Mark as read", description = "Mark all messages in conversation as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Marking messages as read in conversation {} for user {}", conversationId, userId);

        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        messageService.markAsRead(conversationId, userId, isCustomer);

        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    /**
     * Mark messages as delivered
     * PUT /api/v1/conversations/{conversationId}/messages/delivered
     */
    @PutMapping("/delivered")
    @Operation(summary = "Mark as delivered", description = "Mark all messages in conversation as delivered")
    public ResponseEntity<ApiResponse<Void>> markAsDelivered(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Marking messages as delivered in conversation {} for user {}", conversationId, userId);

        messageService.markAsDelivered(conversationId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as delivered"));
    }

    /**
     * Search messages in conversation
     * GET /api/v1/conversations/{conversationId}/messages/search
     */
    @GetMapping("/search")
    @Operation(summary = "Search messages", description = "Search messages in a conversation")
    public ResponseEntity<ApiResponse<PagedResponse<MessageDTO>>> searchMessages(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Searching messages in conversation {} with query: {}", conversationId, query);

        // Validate access
        Conversation conversation = conversationService.findConversationById(conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        conversationService.validateAccess(conversation, userId, isCustomer);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MessageDTO> response = messageService.searchMessages(conversationId, userId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Edit a message
     * PATCH /api/v1/conversations/{conversationId}/messages/{messageId}
     */
    @PatchMapping("/{messageId}")
    @Operation(summary = "Edit message", description = "Edit a sent message (within time limit)")
    public ResponseEntity<ApiResponse<MessageDTO>> editMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String content) {
        log.info("Editing message {} by user {}", messageId, userId);

        MessageDTO message = messageService.editMessage(messageId, userId, content);
        return ResponseEntity.ok(ApiResponse.success(message, "Message edited successfully"));
    }

    /**
     * Delete a message
     * DELETE /api/v1/conversations/{conversationId}/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete message", description = "Delete a sent message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "false") boolean forEveryone) {
        log.info("Deleting message {} by user {} (forEveryone: {})", messageId, userId, forEveryone);

        messageService.deleteMessage(messageId, userId, forEveryone);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }
}
