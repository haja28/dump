package com.makanforyou.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an order from cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCartRequest {

    @NotBlank(message = "Delivery address is required")
    @Size(min = 5, max = 255, message = "Delivery address must be between 5 and 255 characters")
    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @Size(max = 50)
    @JsonProperty("delivery_city")
    private String deliveryCity;

    @Size(max = 50)
    @JsonProperty("delivery_state")
    private String deliveryState;

    @Size(max = 10)
    @JsonProperty("delivery_postal_code")
    private String deliveryPostalCode;

    @Size(max = 500)
    @JsonProperty("special_instructions")
    private String specialInstructions;
}
