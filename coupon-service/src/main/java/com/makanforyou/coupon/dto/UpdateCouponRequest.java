package com.makanforyou.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.coupon.entity.Coupon;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating a coupon (Admin only)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouponRequest {

    @Size(max = 100, message = "Coupon name must be less than 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @JsonProperty("discount_type")
    private Coupon.DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @JsonProperty("discount_value")
    private BigDecimal discountValue;

    @JsonProperty("max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;

    @JsonProperty("max_uses")
    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    @JsonProperty("max_uses_per_user")
    @Min(value = 1, message = "Max uses per user must be at least 1")
    private Integer maxUsesPerUser;

    @JsonProperty("valid_from")
    private LocalDateTime validFrom;

    @JsonProperty("valid_until")
    private LocalDateTime validUntil;

    private Coupon.CouponStatus status;

    @JsonProperty("applicable_to")
    private Coupon.ApplicableTo applicableTo;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("is_first_order_only")
    private Boolean isFirstOrderOnly;

    @JsonProperty("is_new_user_only")
    private Boolean isNewUserOnly;
}
