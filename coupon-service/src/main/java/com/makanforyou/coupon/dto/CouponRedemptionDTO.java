package com.makanforyou.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.makanforyou.coupon.entity.CouponRedemption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Coupon Redemption response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRedemptionDTO {

    @JsonProperty("redemption_id")
    private Long id;

    @JsonProperty("coupon_id")
    private Long couponId;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("coupon_name")
    private String couponName;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("discount_applied")
    private BigDecimal discountApplied;

    private CouponRedemption.RedemptionStatus status;

    @JsonProperty("redeemed_at")
    private LocalDateTime redeemedAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}
