package com.makanforyou.chat.service;

import com.makanforyou.chat.dto.MessageDTO;
import com.makanforyou.chat.dto.SendMessageRequest;
import com.makanforyou.chat.dto.WebSocketMessageDTO;
import com.makanforyou.chat.entity.Conversation;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.exception.ChatAccessDeniedException;
import com.makanforyou.chat.exception.MessageNotFoundException;
import com.makanforyou.chat.mapper.ChatMapper;
import com.makanforyou.chat.repository.ConversationRepository;
import com.makanforyou.chat.repository.MessageRepository;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMapper chatMapper;
    @Lazy
    private final WebSocketService webSocketService;
    @Lazy
    private final NotificationService notificationService;

    /**
     * Send a new message
     */
    @Transactional
    public MessageDTO sendMessage(Long conversationId, Long senderId, Message.SenderType senderType,
                                   String content, Message.MessageType messageType,
                                   String attachmentUrl, String attachmentType) {
        log.info("Sending message to conversation {} from {} {}", conversationId, senderType, senderId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .senderType(senderType)
                .content(content)
                .messageType(messageType)
                .attachmentUrl(attachmentUrl)
                .attachmentType(attachmentType)
                .status(Message.MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);
        log.info("Message {} saved to conversation {}", message.getId(), conversationId);

        // Update conversation
        conversation.setLastMessagePreview(chatMapper.truncateForPreview(content, 100));
        conversation.setLastMessageAt(message.getSentAt());

        // Increment unread count for the other party
        if (senderType == Message.SenderType.CUSTOMER) {
            conversation.setKitchenUnreadCount(conversation.getKitchenUnreadCount() + 1);
        } else {
            conversation.setCustomerUnreadCount(conversation.getCustomerUnreadCount() + 1);
        }
        conversationRepository.save(conversation);

        // Create DTO for API response (with isOwnMessage = true for sender)
        MessageDTO messageDTO = chatMapper.toMessageDTO(message, senderId);

        // Create DTO for WebSocket broadcast (without isOwnMessage so clients determine it themselves)
        MessageDTO broadcastDTO = chatMapper.toMessageDTOForBroadcast(message);

        // Send real-time notification via WebSocket
        try {
            webSocketService.sendMessageToConversation(conversationId, broadcastDTO);

            // Create notification for recipient
            Long recipientId = senderType == Message.SenderType.CUSTOMER ?
                    conversation.getKitchenId() : conversation.getCustomerId();
            Message.SenderType recipientType = senderType == Message.SenderType.CUSTOMER ?
                    Message.SenderType.KITCHEN : Message.SenderType.CUSTOMER;

            notificationService.createNewMessageNotification(
                    recipientId, recipientType, conversationId, message.getId(), content);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }

        return messageDTO;
    }

    /**
     * Send a message from a request DTO
     */
    @Transactional
    public MessageDTO sendMessage(Long conversationId, Long senderId, Message.SenderType senderType, SendMessageRequest request) {
        return sendMessage(
                conversationId,
                senderId,
                senderType,
                request.getContent(),
                request.getMessageType(),
                request.getAttachmentUrl(),
                request.getAttachmentType()
        );
    }

    /**
     * Get messages in a conversation with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<MessageDTO> getMessages(Long conversationId, Long requesterId, Message.SenderType requesterType, Pageable pageable) {
        Page<Message> page = messageRepository.findByConversationIdAndIsDeletedFalseOrderBySentAtDesc(conversationId, pageable);

        List<MessageDTO> messages = page.getContent().stream()
                .map(m -> chatMapper.toMessageDTO(m, requesterId, requesterType))
                .collect(Collectors.toList());

        // Reverse to show oldest first in the list
        java.util.Collections.reverse(messages);

        return PagedResponse.<MessageDTO>builder()
                .content(messages)
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

    /**
     * Get messages (backward compatible)
     * @deprecated Use getMessages(Long, Long, Message.SenderType, Pageable) instead
     */
    @Transactional(readOnly = true)
    public PagedResponse<MessageDTO> getMessages(Long conversationId, Long requesterId, Pageable pageable) {
        return getMessages(conversationId, requesterId, Message.SenderType.CUSTOMER, pageable);
    }

    /**
     * Get all messages in a conversation (for display)
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages(Long conversationId, Long requesterId, Message.SenderType requesterType) {
        return messageRepository.findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(conversationId)
                .stream()
                .map(m -> chatMapper.toMessageDTO(m, requesterId, requesterType))
                .collect(Collectors.toList());
    }

    /**
     * Get all messages (backward compatible - uses old method)
     * @deprecated Use getAllMessages(Long, Long, Message.SenderType) instead
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getAllMessages(Long conversationId, Long requesterId) {
        return messageRepository.findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(conversationId)
                .stream()
                .map(m -> chatMapper.toMessageDTO(m, requesterId))
                .collect(Collectors.toList());
    }

    /**
     * Get messages after a timestamp (for syncing)
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesAfter(Long conversationId, Long requesterId, LocalDateTime after) {
        return messageRepository.findMessagesAfter(conversationId, after)
                .stream()
                .map(m -> chatMapper.toMessageDTO(m, requesterId))
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as delivered
     */
    @Transactional
    public void markAsDelivered(Long conversationId, Long userId) {
        log.info("Marking messages as delivered in conversation {} for user {}", conversationId, userId);
        messageRepository.markMessagesAsDelivered(conversationId, userId, LocalDateTime.now());

        // Send delivery receipts via WebSocket
        try {
            webSocketService.sendDeliveryReceipts(conversationId, userId);
        } catch (Exception e) {
            log.error("Failed to send delivery receipts", e);
        }
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markAsRead(Long conversationId, Long userId, boolean isCustomer) {
        log.info("Marking messages as read in conversation {} for user {}", conversationId, userId);
        messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());

        // Reset unread count
        if (isCustomer) {
            conversationRepository.resetCustomerUnreadCount(conversationId);
        } else {
            conversationRepository.resetKitchenUnreadCount(conversationId);
        }

        // Mark notifications as read
        notificationService.markConversationNotificationsAsRead(conversationId, userId);

        // Send read receipts via WebSocket
        try {
            webSocketService.sendReadReceipts(conversationId, userId);
        } catch (Exception e) {
            log.error("Failed to send read receipts", e);
        }
    }

    /**
     * Mark a single message as read
     */
    @Transactional
    public MessageDTO markMessageAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        if (!message.getSenderId().equals(userId) && message.getStatus() != Message.MessageStatus.READ) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            message = messageRepository.save(message);

            // Send read receipt
            try {
                webSocketService.sendReadReceipt(message.getConversation().getId(), messageId, userId);
            } catch (Exception e) {
                log.error("Failed to send read receipt", e);
            }
        }

        return chatMapper.toMessageDTO(message, userId);
    }

    /**
     * Search messages in a conversation
     */
    @Transactional(readOnly = true)
    public PagedResponse<MessageDTO> searchMessages(Long conversationId, Long requesterId, String query, Pageable pageable) {
        Page<Message> page = messageRepository.searchMessages(conversationId, query, pageable);

        return PagedResponse.<MessageDTO>builder()
                .content(page.getContent().stream()
                        .map(m -> chatMapper.toMessageDTO(m, requesterId))
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

    /**
     * Edit a message
     */
    @Transactional
    public MessageDTO editMessage(Long messageId, Long userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        if (!message.getSenderId().equals(userId)) {
            throw new ChatAccessDeniedException("You can only edit your own messages");
        }

        // Check if message is within edit time limit (e.g., 15 minutes)
        if (message.getSentAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Message can no longer be edited");
        }

        message.setContent(newContent);
        message.setEditedAt(LocalDateTime.now());
        message = messageRepository.save(message);

        log.info("Message {} edited by user {}", messageId, userId);

        return chatMapper.toMessageDTO(message, userId);
    }

    /**
     * Delete a message (soft delete)
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId, boolean forEveryone) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        if (!message.getSenderId().equals(userId)) {
            throw new ChatAccessDeniedException("You can only delete your own messages");
        }

        if (forEveryone) {
            // Check time limit for delete for everyone (e.g., 1 hour)
            if (message.getSentAt().plusHours(1).isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Message can no longer be deleted for everyone");
            }
            messageRepository.softDeleteMessageForEveryone(messageId, LocalDateTime.now());
        } else {
            messageRepository.softDeleteMessage(messageId, LocalDateTime.now());
        }

        log.info("Message {} deleted by user {} (forEveryone: {})", messageId, userId, forEveryone);
    }

    /**
     * Send system message (e.g., order updates)
     */
    @Transactional
    public MessageDTO sendSystemMessage(Long conversationId, String content, Message.MessageType messageType) {
        log.info("Sending system message to conversation {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(0L) // System sender
                .senderType(Message.SenderType.SYSTEM)
                .content(content)
                .messageType(messageType)
                .status(Message.MessageStatus.DELIVERED)
                .build();

        message = messageRepository.save(message);

        // Update conversation
        conversation.setLastMessagePreview(chatMapper.truncateForPreview(content, 100));
        conversation.setLastMessageAt(message.getSentAt());
        conversationRepository.save(conversation);

        MessageDTO messageDTO = chatMapper.toMessageDTO(message, 0L);

        // Send via WebSocket
        try {
            webSocketService.sendMessageToConversation(conversationId, messageDTO);
        } catch (Exception e) {
            log.error("Failed to send system message via WebSocket", e);
        }

        return messageDTO;
    }
}
