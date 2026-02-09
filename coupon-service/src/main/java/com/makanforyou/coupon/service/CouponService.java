package com.makanforyou.coupon.service;

import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.coupon.dto.*;
import com.makanforyou.coupon.entity.Coupon;
import com.makanforyou.coupon.entity.CouponRedemption;
import com.makanforyou.coupon.repository.CouponRedemptionRepository;
import com.makanforyou.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for coupon management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Create a new coupon (Admin only)
     */
    public CouponDTO createCoupon(Long adminId, CreateCouponRequest request) {
        log.info("Creating coupon '{}' by admin {}", request.getCode(), adminId);

        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new ApplicationException("COUPON_EXISTS",
                    "Coupon code '" + request.getCode() + "' already exists");
        }

        // Validate dates
        if (request.getValidFrom() != null && request.getValidUntil() != null) {
            if (request.getValidUntil().isBefore(request.getValidFrom())) {
                throw new ApplicationException("INVALID_DATES",
                        "Valid until date must be after valid from date");
            }
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .maxUses(request.getMaxUses())
                .maxUsesPerUser(request.getMaxUsesPerUser())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .applicableTo(request.getApplicableTo())
                .kitchenId(request.getKitchenId())
                .isFirstOrderOnly(request.getIsFirstOrderOnly())
                .isNewUserOnly(request.getIsNewUserOnly())
                .createdBy(adminId)
                .status(Coupon.CouponStatus.ACTIVE)
                .build();

        coupon = couponRepository.save(coupon);
        log.info("Coupon created with ID: {}", coupon.getId());

        return mapToDTO(coupon);
    }

    /**
     * Update a coupon (Admin only)
     */
    public CouponDTO updateCoupon(Long couponId, UpdateCouponRequest request) {
        log.info("Updating coupon ID: {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException("COUPON_NOT_FOUND", "Coupon not found"));

        if (request.getName() != null) coupon.setName(request.getName());
        if (request.getDescription() != null) coupon.setDescription(request.getDescription());
        if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMaxDiscountAmount() != null) coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getMinOrderAmount() != null) coupon.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getMaxUses() != null) coupon.setMaxUses(request.getMaxUses());
        if (request.getMaxUsesPerUser() != null) coupon.setMaxUsesPerUser(request.getMaxUsesPerUser());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) coupon.setValidUntil(request.getValidUntil());
        if (request.getStatus() != null) coupon.setStatus(request.getStatus());
        if (request.getApplicableTo() != null) coupon.setApplicableTo(request.getApplicableTo());
        if (request.getKitchenId() != null) coupon.setKitchenId(request.getKitchenId());
        if (request.getIsFirstOrderOnly() != null) coupon.setIsFirstOrderOnly(request.getIsFirstOrderOnly());
        if (request.getIsNewUserOnly() != null) coupon.setIsNewUserOnly(request.getIsNewUserOnly());

        coupon = couponRepository.save(coupon);
        log.info("Coupon updated: {}", couponId);

        return mapToDTO(coupon);
    }

    /**
     * Delete a coupon (Admin only) - soft delete by setting status to INACTIVE
     */
    public void deleteCoupon(Long couponId) {
        log.info("Deleting coupon ID: {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException("COUPON_NOT_FOUND", "Coupon not found"));

        coupon.setStatus(Coupon.CouponStatus.INACTIVE);
        couponRepository.save(coupon);

        log.info("Coupon deleted (deactivated): {}", couponId);
    }

    /**
     * Get coupon by ID (Admin)
     */
    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApplicationException("COUPON_NOT_FOUND", "Coupon not found"));
        return mapToDTO(coupon);
    }

    /**
     * Get all coupons with pagination (Admin)
     */
    @Transactional(readOnly = true)
    public PagedResponse<CouponDTO> getAllCoupons(Pageable pageable) {
        Page<Coupon> page = couponRepository.findAll(pageable);
        return buildPagedResponse(page);
    }

    /**
     * Search coupons (Admin)
     */
    @Transactional(readOnly = true)
    public PagedResponse<CouponDTO> searchCoupons(String query, Pageable pageable) {
        Page<Coupon> page = couponRepository.searchCoupons(query, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Get coupons by status (Admin)
     */
    @Transactional(readOnly = true)
    public PagedResponse<CouponDTO> getCouponsByStatus(Coupon.CouponStatus status, Pageable pageable) {
        Page<Coupon> page = couponRepository.findByStatus(status, pageable);
        return buildPagedResponse(page);
    }

    // ==================== CUSTOMER OPERATIONS ====================

    /**
     * Get available coupons for customer
     */
    @Transactional(readOnly = true)
    public List<CouponDTO> getAvailableCoupons(Long kitchenId) {
        List<Coupon> coupons = couponRepository.findAvailableCouponsForCustomer(
                LocalDateTime.now(), kitchenId);
        return coupons.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validate a coupon code (Customer - before checkout)
     */
    @Transactional(readOnly = true)
    public CouponValidationResult validateCoupon(Long userId, ValidateCouponRequest request) {
        log.info("Validating coupon '{}' for user {}", request.getCouponCode(), userId);

        // Find coupon
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode())
                .orElse(null);

        if (coupon == null) {
            return CouponValidationResult.failure("COUPON_NOT_FOUND",
                    "Coupon code '" + request.getCouponCode() + "' not found");
        }

        // Check if coupon is active
        if (coupon.getStatus() != Coupon.CouponStatus.ACTIVE) {
            return CouponValidationResult.failure("COUPON_INACTIVE",
                    "This coupon is no longer active");
        }

        // Check validity period
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return CouponValidationResult.failure("COUPON_NOT_YET_VALID",
                    "This coupon is not valid yet. Valid from: " + coupon.getValidFrom());
        }

        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return CouponValidationResult.failure("COUPON_EXPIRED",
                    "This coupon has expired");
        }

        // Check total usage limit
        if (!coupon.hasUsesRemaining()) {
            return CouponValidationResult.failure("COUPON_EXHAUSTED",
                    "This coupon has reached its maximum usage limit");
        }

        // Check per-user usage limit
        int userUsageCount = redemptionRepository.countUserCouponUsage(userId, coupon.getId());
        if (coupon.getMaxUsesPerUser() != null && userUsageCount >= coupon.getMaxUsesPerUser()) {
            return CouponValidationResult.failure("USER_LIMIT_REACHED",
                    "You have already used this coupon the maximum number of times");
        }

        // Check minimum order amount
        if (coupon.getMinOrderAmount() != null &&
            request.getOrderAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            return CouponValidationResult.minOrderNotMet(
                    coupon.getMinOrderAmount(), request.getOrderAmount());
        }

        // Check kitchen restriction
        if (coupon.getApplicableTo() == Coupon.ApplicableTo.SPECIFIC_KITCHEN) {
            if (request.getKitchenId() == null || !coupon.getKitchenId().equals(request.getKitchenId())) {
                return CouponValidationResult.failure("KITCHEN_MISMATCH",
                        "This coupon is only valid for a specific kitchen");
            }
        }

        // Check first order only
        if (Boolean.TRUE.equals(coupon.getIsFirstOrderOnly()) &&
            !Boolean.TRUE.equals(request.getIsFirstOrder())) {
            return CouponValidationResult.failure("FIRST_ORDER_ONLY",
                    "This coupon is only valid for your first order");
        }

        // Check new user only
        if (Boolean.TRUE.equals(coupon.getIsNewUserOnly()) &&
            !Boolean.TRUE.equals(request.getIsNewUser())) {
            return CouponValidationResult.failure("NEW_USER_ONLY",
                    "This coupon is only valid for new users");
        }

        // Calculate discount
        BigDecimal discountAmount = coupon.calculateDiscount(request.getOrderAmount());
        Integer remainingUses = coupon.getMaxUses() != null
                ? coupon.getMaxUses() - coupon.getCurrentUses() - 1
                : null;

        log.info("Coupon '{}' validated successfully. Discount: {}",
                request.getCouponCode(), discountAmount);

        return CouponValidationResult.success(
                coupon.getCode(),
                coupon.getName(),
                getDiscountDisplay(coupon),
                discountAmount,
                request.getOrderAmount(),
                remainingUses
        );
    }

    /**
     * Redeem a coupon at checkout
     */
    public CouponRedemptionDTO redeemCoupon(Long userId, RedeemCouponRequest request) {
        log.info("Redeeming coupon '{}' for user {} on order {}",
                request.getCouponCode(), userId, request.getOrderId());

        // Validate the coupon first
        ValidateCouponRequest validateRequest = ValidateCouponRequest.builder()
                .couponCode(request.getCouponCode())
                .orderAmount(request.getOrderAmount())
                .kitchenId(request.getKitchenId())
                .build();

        CouponValidationResult validation = validateCoupon(userId, validateRequest);

        if (!validation.getIsValid()) {
            throw new ApplicationException(validation.getErrorCode(), validation.getErrorMessage());
        }

        // Get the coupon
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode())
                .orElseThrow(() -> new ApplicationException("COUPON_NOT_FOUND", "Coupon not found"));

        // Check if already redeemed for this order
        if (redemptionRepository.existsByUserIdAndOrderId(userId, request.getOrderId())) {
            throw new ApplicationException("ALREADY_REDEEMED",
                    "A coupon has already been applied to this order");
        }

        // Create redemption record
        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .userId(userId)
                .orderId(request.getOrderId())
                .orderAmount(request.getOrderAmount())
                .discountApplied(validation.getDiscountAmount())
                .status(CouponRedemption.RedemptionStatus.APPLIED)
                .build();

        redemption = redemptionRepository.save(redemption);

        // Increment coupon usage
        coupon.incrementUsage();
        couponRepository.save(coupon);

        log.info("Coupon redeemed. Redemption ID: {}, Discount: {}",
                redemption.getId(), redemption.getDiscountApplied());

        return mapToRedemptionDTO(redemption);
    }

    /**
     * Complete a coupon redemption (when order is completed)
     */
    public void completeRedemption(Long orderId) {
        log.info("Completing coupon redemption for order {}", orderId);

        redemptionRepository.findByOrderId(orderId).ifPresent(redemption -> {
            redemption.complete();
            redemptionRepository.save(redemption);
            log.info("Redemption completed for order {}", orderId);
        });
    }

    /**
     * Cancel a coupon redemption (when order is cancelled)
     */
    public void cancelRedemption(Long orderId) {
        log.info("Cancelling coupon redemption for order {}", orderId);

        redemptionRepository.findByOrderId(orderId).ifPresent(redemption -> {
            redemption.cancel();
            redemptionRepository.save(redemption);

            // Restore coupon usage count
            Coupon coupon = redemption.getCoupon();
            if (coupon.getCurrentUses() > 0) {
                coupon.setCurrentUses(coupon.getCurrentUses() - 1);
                if (coupon.getStatus() == Coupon.CouponStatus.EXHAUSTED) {
                    coupon.setStatus(Coupon.CouponStatus.ACTIVE);
                }
                couponRepository.save(coupon);
            }

            log.info("Redemption cancelled for order {}", orderId);
        });
    }

    /**
     * Get user's coupon redemption history
     */
    @Transactional(readOnly = true)
    public PagedResponse<CouponRedemptionDTO> getUserRedemptions(Long userId, Pageable pageable) {
        Page<CouponRedemption> page = redemptionRepository.findByUserId(userId, pageable);

        List<CouponRedemptionDTO> content = page.getContent().stream()
                .map(this::mapToRedemptionDTO)
                .collect(Collectors.toList());

        return PagedResponse.<CouponRedemptionDTO>builder()
                .content(content)
                .pagination(PaginationMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }

    // ==================== SCHEDULED TASKS ====================

    /**
     * Scheduled task to expire outdated coupons
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void expireOutdatedCoupons() {
        log.info("Running scheduled task: Expire outdated coupons");
        int expired = couponRepository.expireOutdatedCoupons(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} outdated coupons", expired);
        }
    }

    // ==================== HELPER METHODS ====================

    private String getDiscountDisplay(Coupon coupon) {
        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                String display = coupon.getDiscountValue().stripTrailingZeros().toPlainString() + "% off";
                if (coupon.getMaxDiscountAmount() != null) {
                    display += " (max RM" + coupon.getMaxDiscountAmount() + ")";
                }
                return display;
            case FIXED_AMOUNT:
                return "RM" + coupon.getDiscountValue() + " off";
            case FREE_DELIVERY:
                return "Free Delivery";
            default:
                return "";
        }
    }

    private CouponDTO mapToDTO(Coupon coupon) {
        Integer remainingUses = null;
        if (coupon.getMaxUses() != null) {
            remainingUses = coupon.getMaxUses() - coupon.getCurrentUses();
        }

        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .discountDisplay(getDiscountDisplay(coupon))
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxUses(coupon.getMaxUses())
                .currentUses(coupon.getCurrentUses())
                .remainingUses(remainingUses)
                .maxUsesPerUser(coupon.getMaxUsesPerUser())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .status(coupon.getStatus())
                .applicableTo(coupon.getApplicableTo())
                .kitchenId(coupon.getKitchenId())
                .isFirstOrderOnly(coupon.getIsFirstOrderOnly())
                .isNewUserOnly(coupon.getIsNewUserOnly())
                .isValid(coupon.isValid())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private CouponRedemptionDTO mapToRedemptionDTO(CouponRedemption redemption) {
        return CouponRedemptionDTO.builder()
                .id(redemption.getId())
                .couponId(redemption.getCoupon().getId())
                .couponCode(redemption.getCoupon().getCode())
                .couponName(redemption.getCoupon().getName())
                .userId(redemption.getUserId())
                .orderId(redemption.getOrderId())
                .orderAmount(redemption.getOrderAmount())
                .discountApplied(redemption.getDiscountApplied())
                .status(redemption.getStatus())
                .redeemedAt(redemption.getRedeemedAt())
                .completedAt(redemption.getCompletedAt())
                .build();
    }

    private PagedResponse<CouponDTO> buildPagedResponse(Page<Coupon> page) {
        List<CouponDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PagedResponse.<CouponDTO>builder()
                .content(content)
                .pagination(PaginationMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}
