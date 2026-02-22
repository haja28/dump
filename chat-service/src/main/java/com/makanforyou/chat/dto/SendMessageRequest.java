package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.chat.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending a new message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content must not exceed 5000 characters")
    @JsonProperty("content")
    private String content;

    @JsonProperty("message_type")
    @Builder.Default
    private Message.MessageType messageType = Message.MessageType.TEXT;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    @JsonProperty("attachment_type")
    private String attachmentType;
}
