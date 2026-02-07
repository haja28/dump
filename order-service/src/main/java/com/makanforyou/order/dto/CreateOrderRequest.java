package com.makanforyou.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Request DTO for creating an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Kitchen ID is required")
    private Long kitchenId;

    @NotBlank(message = "Delivery address is required")
    @Size(min = 5, max = 255, message = "Delivery address must be between 5 and 255 characters")
    private String deliveryAddress;

    @Size(max = 50)
    private String deliveryCity;

    @Size(max = 50)
    private String deliveryState;

    @Size(max = 10)
    private String deliveryPostalCode;

    @Size(max = 500)
    private String specialInstructions;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
