package com.makanforyou.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    @JsonProperty("order_id")
    private Long orderId;

    @NotNull(message = "Kitchen ID is required")
    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("initial_message")
    private String initialMessage;
}
