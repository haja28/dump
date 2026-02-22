package com.makanforyou.chat.repository;

import com.makanforyou.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Conversation entity
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find conversation by order ID
     */
    Optional<Conversation> findByOrderId(Long orderId);

    /**
     * Find all conversations for a customer
     */
    Page<Conversation> findByCustomerIdOrderByUpdatedAtDesc(Long customerId, Pageable pageable);

    /**
     * Find all conversations for a kitchen
     */
    Page<Conversation> findByKitchenIdOrderByUpdatedAtDesc(Long kitchenId, Pageable pageable);

    /**
     * Find active conversations for a customer
     */
    Page<Conversation> findByCustomerIdAndStatusOrderByUpdatedAtDesc(
            Long customerId, Conversation.ConversationStatus status, Pageable pageable);

    /**
     * Find active conversations for a kitchen
     */
    Page<Conversation> findByKitchenIdAndStatusOrderByUpdatedAtDesc(
            Long kitchenId, Conversation.ConversationStatus status, Pageable pageable);

    /**
     * Find conversations with unread messages for customer
     */
    @Query("SELECT c FROM Conversation c WHERE c.customerId = :customerId AND c.customerUnreadCount > 0 ORDER BY c.updatedAt DESC")
    Page<Conversation> findUnreadConversationsForCustomer(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find conversations with unread messages for kitchen
     */
    @Query("SELECT c FROM Conversation c WHERE c.kitchenId = :kitchenId AND c.kitchenUnreadCount > 0 ORDER BY c.updatedAt DESC")
    Page<Conversation> findUnreadConversationsForKitchen(@Param("kitchenId") Long kitchenId, Pageable pageable);

    /**
     * Find conversation between customer and kitchen for an order
     */
    Optional<Conversation> findByCustomerIdAndKitchenIdAndOrderId(Long customerId, Long kitchenId, Long orderId);

    /**
     * Count unread conversations for customer
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.customerId = :customerId AND c.customerUnreadCount > 0")
    Long countUnreadConversationsForCustomer(@Param("customerId") Long customerId);

    /**
     * Count unread conversations for kitchen
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.kitchenId = :kitchenId AND c.kitchenUnreadCount > 0")
    Long countUnreadConversationsForKitchen(@Param("kitchenId") Long kitchenId);

    /**
     * Update customer unread count
     */
    @Modifying
    @Query("UPDATE Conversation c SET c.customerUnreadCount = c.customerUnreadCount + 1 WHERE c.id = :conversationId")
    void incrementCustomerUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * Update kitchen unread count
     */
    @Modifying
    @Query("UPDATE Conversation c SET c.kitchenUnreadCount = c.kitchenUnreadCount + 1 WHERE c.id = :conversationId")
    void incrementKitchenUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * Reset customer unread count
     */
    @Modifying
    @Query("UPDATE Conversation c SET c.customerUnreadCount = 0 WHERE c.id = :conversationId")
    void resetCustomerUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * Reset kitchen unread count
     */
    @Modifying
    @Query("UPDATE Conversation c SET c.kitchenUnreadCount = 0 WHERE c.id = :conversationId")
    void resetKitchenUnreadCount(@Param("conversationId") Long conversationId);

    /**
     * Search conversations by title or last message
     */
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.customerId = :userId OR c.kitchenId = :userId) AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.lastMessagePreview) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY c.updatedAt DESC")
    Page<Conversation> searchConversations(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);
}
