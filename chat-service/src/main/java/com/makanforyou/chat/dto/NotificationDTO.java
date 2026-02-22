package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.chat.entity.ChatNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    @JsonProperty("notification_id")
    private Long id;

    @JsonProperty("conversation_id")
    private Long conversationId;

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("type")
    private ChatNotification.NotificationType type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("is_read")
    private Boolean isRead;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("formatted_time")
    private String formattedTime;
}
