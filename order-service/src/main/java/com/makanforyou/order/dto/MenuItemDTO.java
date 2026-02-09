package com.makanforyou.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a menu item with stock information
 * This would typically come from the Menu Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("kitchen_name")
    private String kitchenName;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_description")
    private String itemDescription;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("max_order_quantity")
    private Integer maxOrderQuantity;

    /**
     * Check if the requested quantity is available
     */
    public boolean hasStock(int requestedQuantity) {
        if (!Boolean.TRUE.equals(isAvailable)) {
            return false;
        }
        if (stockQuantity == null) {
            return true; // Unlimited stock
        }
        return stockQuantity >= requestedQuantity;
    }

    /**
     * Check if quantity exceeds max order limit
     */
    public boolean exceedsMaxOrder(int requestedQuantity) {
        if (maxOrderQuantity == null) {
            return false; // No limit
        }
        return requestedQuantity > maxOrderQuantity;
    }
}
