package com.makanforyou.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Cart response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    @JsonProperty("cart_id")
    private Long cartId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("kitchen_name")
    private String kitchenName;

    private List<CartItemDTO> items;

    private BigDecimal subtotal;

    @JsonProperty("delivery_fee")
    private BigDecimal deliveryFee;

    private BigDecimal discount;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("coupon_description")
    private String couponDescription;

    private BigDecimal total;

    @JsonProperty("item_count")
    private Integer itemCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Validation and warnings
    @JsonProperty("is_valid")
    @Builder.Default
    private Boolean isValid = true;

    @JsonProperty("has_stock_issues")
    @Builder.Default
    private Boolean hasStockIssues = false;

    @JsonProperty("has_price_changes")
    @Builder.Default
    private Boolean hasPriceChanges = false;

    @JsonProperty("warnings")
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("minutes_until_expiry")
    private Long minutesUntilExpiry;
}
