package com.makanforyou.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Coupon response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {

    @JsonProperty("coupon_id")
    private Long id;

    private String code;

    private String name;

    private String description;

    @JsonProperty("discount_type")
    private Coupon.DiscountType discountType;

    @JsonProperty("discount_value")
    private BigDecimal discountValue;

    @JsonProperty("discount_display")
    private String discountDisplay;

    @JsonProperty("max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;

    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("current_uses")
    private Integer currentUses;

    @JsonProperty("remaining_uses")
    private Integer remainingUses;

    @JsonProperty("max_uses_per_user")
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

    @JsonProperty("is_valid")
    private Boolean isValid;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
