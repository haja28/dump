package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.chat.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket message events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {

    @JsonProperty("event_type")
    private EventType eventType;

    @JsonProperty("conversation_id")
    private Long conversationId;

    @JsonProperty("message")
    private MessageDTO message;

    @JsonProperty("typing_indicator")
    private TypingIndicatorDTO typingIndicator;

    @JsonProperty("read_receipt")
    private ReadReceiptDTO readReceipt;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Event types for WebSocket
     */
    public enum EventType {
        NEW_MESSAGE,
        MESSAGE_DELIVERED,
        MESSAGE_READ,
        TYPING_START,
        TYPING_STOP,
        USER_ONLINE,
        USER_OFFLINE,
        CONVERSATION_UPDATED
    }

    /**
     * Read receipt DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadReceiptDTO {
        @JsonProperty("message_id")
        private Long messageId;

        @JsonProperty("reader_id")
        private Long readerId;

        @JsonProperty("read_at")
        private LocalDateTime readAt;

        @JsonProperty("status")
        private Message.MessageStatus status;
    }
}
