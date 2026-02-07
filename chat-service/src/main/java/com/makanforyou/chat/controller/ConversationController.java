package com.makanforyou.chat.controller;

import com.makanforyou.chat.dto.ConversationDTO;
import com.makanforyou.chat.dto.CreateConversationRequest;
import com.makanforyou.chat.entity.Conversation;
import com.makanforyou.chat.service.ConversationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for conversation management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Chat conversation management endpoints")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Create a new conversation (Customer)
     * POST /api/v1/conversations
     */
    @PostMapping
    @Operation(summary = "Create conversation", description = "Customer initiates a new chat conversation")
    public ResponseEntity<ApiResponse<ConversationDTO>> createConversation(
            @Parameter(description = "Customer ID from auth header")
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody CreateConversationRequest request) {
        log.info("Creating conversation for customer {} with kitchen {}", customerId, request.getKitchenId());
        ConversationDTO conversation = conversationService.createConversation(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversation, "Conversation created successfully"));
    }

    /**
     * Get conversation by ID
     * GET /api/v1/conversations/{conversationId}
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation", description = "Get conversation details by ID")
    public ResponseEntity<ApiResponse<ConversationDTO>> getConversation(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Getting conversation {} for user {}", conversationId, userId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        ConversationDTO conversation = conversationService.getConversation(conversationId, userId, isCustomer);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    /**
     * Get customer's conversations
     * GET /api/v1/conversations/customer
     */
    @GetMapping("/customer")
    @Operation(summary = "Get customer conversations", description = "Get all conversations for a customer")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> getCustomerConversations(
            @RequestHeader("X-User-Id") Long customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching conversations for customer {}", customerId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ConversationDTO> response = conversationService.getCustomerConversations(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get kitchen's conversations
     * GET /api/v1/conversations/kitchen
     */
    @GetMapping("/kitchen")
    @Operation(summary = "Get kitchen conversations", description = "Get all conversations for a kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> getKitchenConversations(
            @RequestHeader("X-Kitchen-Id") Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching conversations for kitchen {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ConversationDTO> response = conversationService.getKitchenConversations(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get active conversations for customer
     * GET /api/v1/conversations/customer/active
     */
    @GetMapping("/customer/active")
    @Operation(summary = "Get active customer conversations", description = "Get active conversations for a customer")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> getActiveCustomerConversations(
            @RequestHeader("X-User-Id") Long customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching active conversations for customer {}", customerId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ConversationDTO> response = conversationService.getActiveCustomerConversations(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get active conversations for kitchen
     * GET /api/v1/conversations/kitchen/active
     */
    @GetMapping("/kitchen/active")
    @Operation(summary = "Get active kitchen conversations", description = "Get active conversations for a kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> getActiveKitchenConversations(
            @RequestHeader("X-Kitchen-Id") Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching active conversations for kitchen {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ConversationDTO> response = conversationService.getActiveKitchenConversations(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread conversations
     * GET /api/v1/conversations/unread
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread conversations", description = "Get conversations with unread messages")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> getUnreadConversations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching unread conversations for {} {}", userType, userId);
        Pageable pageable = PageRequest.of(page, size);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        PagedResponse<ConversationDTO> response;
        if (isCustomer) {
            response = conversationService.getUnreadCustomerConversations(userId, pageable);
        } else {
            response = conversationService.getUnreadKitchenConversations(userId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search conversations
     * GET /api/v1/conversations/search
     */
    @GetMapping("/search")
    @Operation(summary = "Search conversations", description = "Search conversations by title or last message")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDTO>>> searchConversations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Searching conversations for {} {} with query: {}", userType, userId, query);
        Pageable pageable = PageRequest.of(page, size);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        PagedResponse<ConversationDTO> response = conversationService.searchConversations(userId, query, isCustomer, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update conversation status
     * PATCH /api/v1/conversations/{conversationId}/status
     */
    @PatchMapping("/{conversationId}/status")
    @Operation(summary = "Update status", description = "Update conversation status")
    public ResponseEntity<ApiResponse<ConversationDTO>> updateStatus(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam Conversation.ConversationStatus status) {
        log.info("Updating conversation {} status to {}", conversationId, status);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        ConversationDTO conversation = conversationService.updateStatus(conversationId, userId, isCustomer, status);
        return ResponseEntity.ok(ApiResponse.success(conversation, "Conversation status updated"));
    }

    /**
     * Archive conversation
     * POST /api/v1/conversations/{conversationId}/archive
     */
    @PostMapping("/{conversationId}/archive")
    @Operation(summary = "Archive conversation", description = "Archive a conversation")
    public ResponseEntity<ApiResponse<ConversationDTO>> archiveConversation(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Archiving conversation {}", conversationId);
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        ConversationDTO conversation = conversationService.archiveConversation(conversationId, userId, isCustomer);
        return ResponseEntity.ok(ApiResponse.success(conversation, "Conversation archived"));
    }

    /**
     * Get unread conversation count
     * GET /api/v1/conversations/unread/count
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of conversations with unread messages")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userType);
        Long count = conversationService.countUnreadConversations(userId, isCustomer);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
