package com.makanforyou.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CouponRedemption Entity - Tracks coupon usage by users
 */
@Entity
@Table(name = "coupon_redemptions", indexes = {
        @Index(name = "idx_redemption_coupon", columnList = "coupon_id"),
        @Index(name = "idx_redemption_user", columnList = "user_id"),
        @Index(name = "idx_redemption_order", columnList = "order_id"),
        @Index(name = "idx_redemption_user_coupon", columnList = "user_id, coupon_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(precision = 10, scale = 2, name = "order_amount")
    private BigDecimal orderAmount;

    @Column(precision = 10, scale = 2, name = "discount_applied")
    private BigDecimal discountApplied;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RedemptionStatus status = RedemptionStatus.APPLIED;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        redeemedAt = LocalDateTime.now();
    }

    /**
     * Redemption status
     */
    public enum RedemptionStatus {
        APPLIED,    // Coupon applied to cart/checkout
        COMPLETED,  // Order completed with coupon
        CANCELLED,  // Order was cancelled, coupon returned
        EXPIRED     // Coupon reservation expired
    }

    /**
     * Mark redemption as completed
     */
    public void complete() {
        this.status = RedemptionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark redemption as cancelled (return coupon)
     */
    public void cancel() {
        this.status = RedemptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}
