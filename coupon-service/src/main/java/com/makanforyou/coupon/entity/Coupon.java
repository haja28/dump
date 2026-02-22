package com.makanforyou.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon Entity - Represents a discount coupon
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true),
        @Index(name = "idx_coupon_status", columnList = "status"),
        @Index(name = "idx_coupon_valid_dates", columnList = "valid_from, valid_until")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "discount_type", length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2, name = "discount_value")
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2, name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(precision = 10, scale = 2, name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "current_uses")
    @Builder.Default
    private Integer currentUses = 0;

    @Column(name = "max_uses_per_user")
    @Builder.Default
    private Integer maxUsesPerUser = 1;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to", length = 20)
    @Builder.Default
    private ApplicableTo applicableTo = ApplicableTo.ALL;

    @Column(name = "kitchen_id")
    private Long kitchenId;

    @Column(name = "is_first_order_only")
    @Builder.Default
    private Boolean isFirstOrderOnly = false;

    @Column(name = "is_new_user_only")
    @Builder.Default
    private Boolean isNewUserOnly = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Discount types
     */
    public enum DiscountType {
        PERCENTAGE,      // e.g., 10% off
        FIXED_AMOUNT,    // e.g., RM 5 off
        FREE_DELIVERY    // Free delivery
    }

    /**
     * Coupon status
     */
    public enum CouponStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED,
        EXHAUSTED
    }

    /**
     * Who the coupon applies to
     */
    public enum ApplicableTo {
        ALL,            // All orders
        SPECIFIC_KITCHEN, // Only specific kitchen
        DELIVERY_ONLY,  // Only delivery orders
        PICKUP_ONLY     // Only pickup orders
    }

    /**
     * Check if coupon is currently valid
     */
    public boolean isValid() {
        if (status != CouponStatus.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        if (maxUses != null && currentUses >= maxUses) {
            return false;
        }

        return true;
    }

    /**
     * Check if coupon has uses remaining
     */
    public boolean hasUsesRemaining() {
        if (maxUses == null) {
            return true; // Unlimited
        }
        return currentUses < maxUses;
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.currentUses++;
        if (maxUses != null && currentUses >= maxUses) {
            this.status = CouponStatus.EXHAUSTED;
        }
    }

    /**
     * Calculate discount amount for an order
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid()) {
            return BigDecimal.ZERO;
        }

        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        switch (discountType) {
            case PERCENTAGE:
                discount = orderAmount.multiply(discountValue).divide(new BigDecimal("100"));
                break;
            case FIXED_AMOUNT:
                discount = discountValue;
                break;
            case FREE_DELIVERY:
                discount = discountValue; // Delivery fee amount
                break;
            default:
                discount = BigDecimal.ZERO;
        }

        // Apply max discount cap if set
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        // Ensure discount doesn't exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }
}
