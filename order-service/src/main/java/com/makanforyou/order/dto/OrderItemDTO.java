package com.makanforyou.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO for OrderItem response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long itemId;
    private Integer itemQuantity;
    private BigDecimal itemUnitPrice;
    private BigDecimal itemTotal;
    private String specialRequests;
}
