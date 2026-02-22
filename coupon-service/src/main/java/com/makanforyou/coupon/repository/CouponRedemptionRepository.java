package com.makanforyou.coupon.repository;

import com.makanforyou.coupon.entity.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CouponRedemption entity
 */
@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    /**
     * Find redemption by order ID
     */
    Optional<CouponRedemption> findByOrderId(Long orderId);

    /**
     * Find all redemptions for a user
     */
    List<CouponRedemption> findByUserId(Long userId);

    /**
     * Find all redemptions for a user with pagination
     */
    Page<CouponRedemption> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all redemptions for a coupon
     */
    List<CouponRedemption> findByCouponId(Long couponId);

    /**
     * Count how many times a user has used a specific coupon
     */
    @Query("SELECT COUNT(r) FROM CouponRedemption r " +
           "WHERE r.userId = :userId AND r.coupon.id = :couponId " +
           "AND r.status IN ('APPLIED', 'COMPLETED')")
    int countUserCouponUsage(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * Check if user has already used coupon for an order
     */
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);

    /**
     * Find pending redemptions (applied but not completed)
     */
    List<CouponRedemption> findByStatus(CouponRedemption.RedemptionStatus status);

    /**
     * Find redemption by user, coupon, and status
     */
    Optional<CouponRedemption> findByUserIdAndCouponIdAndStatus(
            Long userId, Long couponId, CouponRedemption.RedemptionStatus status);

    /**
     * Get total discount given for a coupon
     */
    @Query("SELECT COALESCE(SUM(r.discountApplied), 0) FROM CouponRedemption r " +
           "WHERE r.coupon.id = :couponId AND r.status = 'COMPLETED'")
    java.math.BigDecimal getTotalDiscountByCoupon(@Param("couponId") Long couponId);

    /**
     * Get redemption count by coupon
     */
    long countByCouponIdAndStatus(Long couponId, CouponRedemption.RedemptionStatus status);
}
