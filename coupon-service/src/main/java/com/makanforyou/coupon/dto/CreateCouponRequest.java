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
 * Request DTO for creating a coupon (Admin only)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Coupon code must be uppercase alphanumeric with underscores or hyphens only")
    private String code;

    @NotBlank(message = "Coupon name is required")
    @Size(max = 100, message = "Coupon name must be less than 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    @JsonProperty("discount_type")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
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
    @Builder.Default
    private Integer maxUsesPerUser = 1;

    @JsonProperty("valid_from")
    private LocalDateTime validFrom;

    @JsonProperty("valid_until")
    private LocalDateTime validUntil;

    @JsonProperty("applicable_to")
    @Builder.Default
    private Coupon.ApplicableTo applicableTo = Coupon.ApplicableTo.ALL;

    @JsonProperty("kitchen_id")
    private Long kitchenId;

    @JsonProperty("is_first_order_only")
    @Builder.Default
    private Boolean isFirstOrderOnly = false;

    @JsonProperty("is_new_user_only")
    @Builder.Default
    private Boolean isNewUserOnly = false;
}
