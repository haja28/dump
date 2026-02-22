package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.chat.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Message response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    @JsonProperty("message_id")
    private Long id;

    @JsonProperty("conversation_id")
    private Long conversationId;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("sender_type")
    private Message.SenderType senderType;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_avatar")
    private String senderAvatar;

    @JsonProperty("content")
    private String content;

    @JsonProperty("message_type")
    private Message.MessageType messageType;

    @JsonProperty("status")
    private Message.MessageStatus status;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    @JsonProperty("attachment_type")
    private String attachmentType;

    @JsonProperty("sent_at")
    private LocalDateTime sentAt;

    @JsonProperty("delivered_at")
    private LocalDateTime deliveredAt;

    @JsonProperty("read_at")
    private LocalDateTime readAt;

    @JsonProperty("edited_at")
    private LocalDateTime editedAt;

    @JsonProperty("is_edited")
    private Boolean isEdited;

    @JsonProperty("is_own_message")
    private Boolean isOwnMessage;

    @JsonProperty("formatted_time")
    private String formattedTime;
}
