package com.makanforyou.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for coupon validation result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResult {

    @JsonProperty("is_valid")
    private Boolean isValid;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("coupon_name")
    private String couponName;

    @JsonProperty("discount_display")
    private String discountDisplay;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("final_amount")
    private BigDecimal finalAmount;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;

    @JsonProperty("remaining_uses")
    private Integer remainingUses;

    /**
     * Create a successful validation result
     */
    public static CouponValidationResult success(String couponCode, String couponName,
                                                  String discountDisplay, BigDecimal discountAmount,
                                                  BigDecimal orderAmount, Integer remainingUses) {
        return CouponValidationResult.builder()
                .isValid(true)
                .couponCode(couponCode)
                .couponName(couponName)
                .discountDisplay(discountDisplay)
                .discountAmount(discountAmount)
                .orderAmount(orderAmount)
                .finalAmount(orderAmount.subtract(discountAmount))
                .remainingUses(remainingUses)
                .build();
    }

    /**
     * Create a failed validation result
     */
    public static CouponValidationResult failure(String errorCode, String errorMessage) {
        return CouponValidationResult.builder()
                .isValid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Create a failed validation with min order info
     */
    public static CouponValidationResult minOrderNotMet(BigDecimal minOrderAmount, BigDecimal orderAmount) {
        return CouponValidationResult.builder()
                .isValid(false)
                .errorCode("MIN_ORDER_NOT_MET")
                .errorMessage("Minimum order amount of RM" + minOrderAmount + " required")
                .minOrderAmount(minOrderAmount)
                .orderAmount(orderAmount)
                .build();
    }
}
