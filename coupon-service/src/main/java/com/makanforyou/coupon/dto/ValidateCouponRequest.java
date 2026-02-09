package com.makanforyou.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for validating/applying a coupon
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @JsonProperty("coupon_code")
    private String couponCode;

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01", message = "Order amount must be greater than 0")
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("is_first_order")
    private Boolean isFirstOrder;

    @JsonProperty("is_new_user")
    private Boolean isNewUser;

    @JsonProperty("is_delivery")
    private Boolean isDelivery;
}
