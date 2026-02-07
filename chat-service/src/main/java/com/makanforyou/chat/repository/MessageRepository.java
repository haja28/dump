package com.makanforyou.chat.repository;

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
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a conversation ordered by sent time (newest first for pagination)
     */
    Page<Message> findByConversationIdAndIsDeletedFalseOrderBySentAtDesc(Long conversationId, Pageable pageable);

    /**
     * Find all messages in a conversation ordered by sent time (oldest first for display)
     */
    List<Message> findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(Long conversationId);

    /**
     * Find messages after a certain timestamp
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sentAt > :after AND m.isDeleted = false ORDER BY m.sentAt ASC")
    List<Message> findMessagesAfter(@Param("conversationId") Long conversationId, @Param("after") LocalDateTime after);

    /**
     * Find unread messages for a user in a conversation
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.status != 'READ' AND m.isDeleted = false ORDER BY m.sentAt ASC")
    List<Message> findUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Count unread messages for a user in a conversation
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.status != 'READ' AND m.isDeleted = false")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Mark messages as delivered
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = 'DELIVERED', m.deliveredAt = :deliveredAt WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.status = 'SENT'")
    void markMessagesAsDelivered(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("deliveredAt") LocalDateTime deliveredAt);

    /**
     * Mark messages as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = 'READ', m.readAt = :readAt WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.status != 'READ'")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Search messages in a conversation
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isDeleted = false AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.sentAt DESC")
    Page<Message> searchMessages(@Param("conversationId") Long conversationId, @Param("query") String query, Pageable pageable);

    /**
     * Get latest message in a conversation
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isDeleted = false ORDER BY m.sentAt DESC LIMIT 1")
    Message findLatestMessage(@Param("conversationId") Long conversationId);

    /**
     * Find messages by type in a conversation
     */
    Page<Message> findByConversationIdAndMessageTypeAndIsDeletedFalseOrderBySentAtDesc(
            Long conversationId, Message.MessageType messageType, Pageable pageable);

    /**
     * Soft delete a message
     */
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedAt = :deletedAt WHERE m.id = :messageId")
    void softDeleteMessage(@Param("messageId") Long messageId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * Soft delete a message for everyone
     */
    @Modifying
    @Query("UPDATE Message m SET m.isDeleted = true, m.deletedForEveryone = true, m.deletedAt = :deletedAt WHERE m.id = :messageId")
    void softDeleteMessageForEveryone(@Param("messageId") Long messageId, @Param("deletedAt") LocalDateTime deletedAt);
}
