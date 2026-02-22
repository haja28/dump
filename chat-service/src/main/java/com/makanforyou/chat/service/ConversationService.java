package com.makanforyou.chat.service;

import com.makanforyou.chat.dto.ConversationDTO;
import com.makanforyou.chat.dto.CreateConversationRequest;
import com.makanforyou.chat.entity.Conversation;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.exception.ChatAccessDeniedException;
import com.makanforyou.chat.exception.ConversationNotFoundException;
import com.makanforyou.chat.mapper.ChatMapper;
import com.makanforyou.chat.repository.ConversationRepository;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing conversations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMapper chatMapper;
    private final MessageService messageService;

    /**
     * Create a new conversation
     */
    @Transactional
    public ConversationDTO createConversation(Long customerId, CreateConversationRequest request) {
        log.info("Creating conversation for customer {} with kitchen {}", customerId, request.getKitchenId());

        // Check if conversation already exists for this order
        if (request.getOrderId() != null) {
            Optional<Conversation> existing = conversationRepository
                    .findByCustomerIdAndKitchenIdAndOrderId(customerId, request.getKitchenId(), request.getOrderId());
            if (existing.isPresent()) {
                log.info("Conversation already exists for order {}", request.getOrderId());
                return chatMapper.toConversationDTO(existing.get(), customerId, true);
            }
        }

        // Create new conversation
        Conversation conversation = Conversation.builder()
                .customerId(customerId)
                .kitchenId(request.getKitchenId())
                .orderId(request.getOrderId())
                .title(request.getTitle() != null ? request.getTitle() : "Order Chat")
                .status(Conversation.ConversationStatus.ACTIVE)
                .build();

        conversation = conversationRepository.save(conversation);
        log.info("Created conversation with id {}", conversation.getId());

        // Send initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            messageService.sendMessage(
                    conversation.getId(),
                    customerId,
                    Message.SenderType.CUSTOMER,
                    request.getInitialMessage(),
                    Message.MessageType.TEXT,
                    null,
                    null
            );
        }

        return chatMapper.toConversationDTO(conversation, customerId, true);
    }

    /**
     * Get conversation by ID
     */
    @Transactional(readOnly = true)
    public ConversationDTO getConversation(Long conversationId, Long userId, boolean isCustomer) {
        Conversation conversation = findConversationById(conversationId);
        validateAccess(conversation, userId, isCustomer);
        return chatMapper.toConversationDTO(conversation, userId, isCustomer);
    }

    /**
     * Get all conversations for a customer
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getCustomerConversations(Long customerId, Pageable pageable) {
        Page<Conversation> page = conversationRepository.findByCustomerIdOrderByUpdatedAtDesc(customerId, pageable);
        return toPagedResponse(page, customerId, true);
    }

    /**
     * Get all conversations for a kitchen
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getKitchenConversations(Long kitchenId, Pageable pageable) {
        Page<Conversation> page = conversationRepository.findByKitchenIdOrderByUpdatedAtDesc(kitchenId, pageable);
        return toPagedResponse(page, kitchenId, false);
    }

    /**
     * Get active conversations for a customer
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getActiveCustomerConversations(Long customerId, Pageable pageable) {
        Page<Conversation> page = conversationRepository
                .findByCustomerIdAndStatusOrderByUpdatedAtDesc(customerId, Conversation.ConversationStatus.ACTIVE, pageable);
        return toPagedResponse(page, customerId, true);
    }

    /**
     * Get active conversations for a kitchen
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getActiveKitchenConversations(Long kitchenId, Pageable pageable) {
        Page<Conversation> page = conversationRepository
                .findByKitchenIdAndStatusOrderByUpdatedAtDesc(kitchenId, Conversation.ConversationStatus.ACTIVE, pageable);
        return toPagedResponse(page, kitchenId, false);
    }

    /**
     * Get unread conversations for a customer
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getUnreadCustomerConversations(Long customerId, Pageable pageable) {
        Page<Conversation> page = conversationRepository.findUnreadConversationsForCustomer(customerId, pageable);
        return toPagedResponse(page, customerId, true);
    }

    /**
     * Get unread conversations for a kitchen
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> getUnreadKitchenConversations(Long kitchenId, Pageable pageable) {
        Page<Conversation> page = conversationRepository.findUnreadConversationsForKitchen(kitchenId, pageable);
        return toPagedResponse(page, kitchenId, false);
    }

    /**
     * Search conversations
     */
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDTO> searchConversations(Long userId, String query, boolean isCustomer, Pageable pageable) {
        Page<Conversation> page = conversationRepository.searchConversations(userId, query, pageable);
        return toPagedResponse(page, userId, isCustomer);
    }

    /**
     * Update conversation status
     */
    @Transactional
    public ConversationDTO updateStatus(Long conversationId, Long userId, boolean isCustomer, Conversation.ConversationStatus status) {
        Conversation conversation = findConversationById(conversationId);
        validateAccess(conversation, userId, isCustomer);

        conversation.setStatus(status);
        if (status == Conversation.ConversationStatus.ARCHIVED) {
            conversation.setArchivedAt(LocalDateTime.now());
        }

        conversation = conversationRepository.save(conversation);
        log.info("Updated conversation {} status to {}", conversationId, status);

        return chatMapper.toConversationDTO(conversation, userId, isCustomer);
    }

    /**
     * Archive a conversation
     */
    @Transactional
    public ConversationDTO archiveConversation(Long conversationId, Long userId, boolean isCustomer) {
        return updateStatus(conversationId, userId, isCustomer, Conversation.ConversationStatus.ARCHIVED);
    }

    /**
     * Update last message preview
     */
    @Transactional
    public void updateLastMessage(Long conversationId, String preview, LocalDateTime timestamp) {
        Conversation conversation = findConversationById(conversationId);
        conversation.setLastMessagePreview(chatMapper.truncateForPreview(preview, 100));
        conversation.setLastMessageAt(timestamp);
        conversationRepository.save(conversation);
    }

    /**
     * Increment unread count
     */
    @Transactional
    public void incrementUnreadCount(Long conversationId, boolean forCustomer) {
        if (forCustomer) {
            conversationRepository.incrementCustomerUnreadCount(conversationId);
        } else {
            conversationRepository.incrementKitchenUnreadCount(conversationId);
        }
    }

    /**
     * Reset unread count
     */
    @Transactional
    public void resetUnreadCount(Long conversationId, boolean forCustomer) {
        if (forCustomer) {
            conversationRepository.resetCustomerUnreadCount(conversationId);
        } else {
            conversationRepository.resetKitchenUnreadCount(conversationId);
        }
    }

    /**
     * Count unread conversations
     */
    @Transactional(readOnly = true)
    public Long countUnreadConversations(Long userId, boolean isCustomer) {
        if (isCustomer) {
            return conversationRepository.countUnreadConversationsForCustomer(userId);
        }
        return conversationRepository.countUnreadConversationsForKitchen(userId);
    }

    /**
     * Find conversation by ID
     */
    public Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    }

    /**
     * Validate user has access to conversation
     */
    public void validateAccess(Conversation conversation, Long userId, boolean isCustomer) {
        if (isCustomer) {
            if (!conversation.getCustomerId().equals(userId)) {
                throw new ChatAccessDeniedException(conversation.getId(), userId);
            }
        } else {
            if (!conversation.getKitchenId().equals(userId)) {
                throw new ChatAccessDeniedException(conversation.getId(), userId);
            }
        }
    }

    /**
     * Convert page to paged response
     */
    private PagedResponse<ConversationDTO> toPagedResponse(Page<Conversation> page, Long userId, boolean isCustomer) {
        return PagedResponse.<ConversationDTO>builder()
                .content(page.getContent().stream()
                        .map(c -> chatMapper.toConversationDTO(c, userId, isCustomer))
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
