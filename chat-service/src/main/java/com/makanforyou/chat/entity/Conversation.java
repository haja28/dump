package com.makanforyou.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Conversation Entity - Represents a chat thread between Customer and Kitchen
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversation_order", columnList = "order_id"),
        @Index(name = "idx_conversation_customer", columnList = "customer_id"),
        @Index(name = "idx_conversation_kitchen", columnList = "kitchen_id"),
        @Index(name = "idx_conversation_status", columnList = "status"),
        @Index(name = "idx_conversation_updated", columnList = "updated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false, name = "customer_id")
    private Long customerId;

    @Column(nullable = false, name = "kitchen_id")
    private Long kitchenId;

    @Column(name = "kitchen_user_id")
    private Long kitchenUserId;

    @Column(length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "customer_unread_count")
    @Builder.Default
    private Integer customerUnreadCount = 0;

    @Column(name = "kitchen_unread_count")
    @Builder.Default
    private Integer kitchenUnreadCount = 0;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Conversation status enum
     */
    public enum ConversationStatus {
        ACTIVE,
        WAITING,
        RESOLVED,
        ARCHIVED
    }
}
