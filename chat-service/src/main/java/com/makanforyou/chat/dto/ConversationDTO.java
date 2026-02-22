package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.chat.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Conversation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    @JsonProperty("conversation_id")
    private Long id;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("kitchen_user_id")
    private Long kitchenUserId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("status")
    private Conversation.ConversationStatus status;

    @JsonProperty("unread_count")
    private Integer unreadCount;

    @JsonProperty("last_message_preview")
    private String lastMessagePreview;

    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;

    @JsonProperty("other_party_name")
    private String otherPartyName;

    @JsonProperty("other_party_avatar")
    private String otherPartyAvatar;

    @JsonProperty("other_party_online")
    private Boolean otherPartyOnline;

    @JsonProperty("other_party_last_seen")
    private LocalDateTime otherPartyLastSeen;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
