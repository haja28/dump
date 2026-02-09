package com.makanforyou.coupon.repository;

import com.makanforyou.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Coupon entity
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Find coupon by code
     */
    Optional<Coupon> findByCode(String code);

    /**
     * Find coupon by code (case insensitive)
     */
    Optional<Coupon> findByCodeIgnoreCase(String code);

    /**
     * Check if coupon code exists
     */
    boolean existsByCode(String code);

    /**
     * Find all active coupons
     */
    List<Coupon> findByStatus(Coupon.CouponStatus status);

    /**
     * Find all active coupons with pagination
     */
    Page<Coupon> findByStatus(Coupon.CouponStatus status, Pageable pageable);

    /**
     * Find active coupons valid at current time
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND (c.validFrom IS NULL OR c.validFrom <= :now) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= :now) " +
           "AND (c.maxUses IS NULL OR c.currentUses < c.maxUses)")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);

    /**
     * Find coupons for a specific kitchen
     */
    List<Coupon> findByKitchenIdAndStatus(Long kitchenId, Coupon.CouponStatus status);

    /**
     * Find coupons created by admin
     */
    Page<Coupon> findByCreatedBy(Long adminId, Pageable pageable);

    /**
     * Update expired coupons status
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXPIRED' " +
           "WHERE c.status = 'ACTIVE' AND c.validUntil < :now")
    int expireOutdatedCoupons(@Param("now") LocalDateTime now);

    /**
     * Update exhausted coupons status
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXHAUSTED' " +
           "WHERE c.status = 'ACTIVE' AND c.maxUses IS NOT NULL AND c.currentUses >= c.maxUses")
    int markExhaustedCoupons();

    /**
     * Search coupons by code or name
     */
    @Query("SELECT c FROM Coupon c WHERE " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Coupon> searchCoupons(@Param("query") String query, Pageable pageable);

    /**
     * Find public coupons that customers can see/claim
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND (c.validFrom IS NULL OR c.validFrom <= :now) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= :now) " +
           "AND (c.maxUses IS NULL OR c.currentUses < c.maxUses) " +
           "AND (c.kitchenId IS NULL OR c.kitchenId = :kitchenId)")
    List<Coupon> findAvailableCouponsForCustomer(@Param("now") LocalDateTime now,
                                                   @Param("kitchenId") Long kitchenId);
}
