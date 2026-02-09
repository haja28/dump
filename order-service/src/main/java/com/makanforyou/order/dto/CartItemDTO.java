package com.makanforyou.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for CartItem response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    @JsonProperty("cart_item_id")
    private Long cartItemId;

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_description")
    private String itemDescription;

    private Integer quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("special_requests")
    private String specialRequests;

    @JsonProperty("image_url")
    private String imageUrl;

    // Stock information
    @JsonProperty("in_stock")
    private Boolean inStock;

    @JsonProperty("available_stock")
    private Integer availableStock;

    @JsonProperty("stock_warning")
    private String stockWarning;

    // Price change information
    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @JsonProperty("price_changed")
    private Boolean priceChanged;

    @JsonProperty("price_difference")
    private BigDecimal priceDifference;

    @JsonProperty("price_change_message")
    private String priceChangeMessage;

    @JsonProperty("added_at")
    private LocalDateTime addedAt;
}
