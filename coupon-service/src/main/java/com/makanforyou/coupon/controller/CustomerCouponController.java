package com.makanforyou.coupon.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.coupon.dto.*;
import com.makanforyou.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer Controller for coupon operations
 * Customers can view, validate, and redeem coupons
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon endpoints for customers")
public class CustomerCouponController {

    private final CouponService couponService;

    /**
     * Get available coupons for a kitchen
     * GET /api/v1/coupons/available
     */
    @GetMapping("/available")
    @Operation(summary = "Get available coupons",
               description = "Get list of available coupons for a kitchen or all coupons")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAvailableCoupons(
            @RequestParam(name = "kitchen_id", required = false) Long kitchenId) {
        log.info("Getting available coupons for kitchen: {}", kitchenId);
        List<CouponDTO> coupons = couponService.getAvailableCoupons(kitchenId);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * Validate a coupon code
     * POST /api/v1/coupons/validate
     *
     * Use this before checkout to check if a coupon is valid and see the discount amount
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate coupon",
               description = "Validate a coupon code and calculate discount amount")
    public ResponseEntity<ApiResponse<CouponValidationResult>> validateCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ValidateCouponRequest request) {
        log.info("User {} validating coupon: {}", userId, request.getCouponCode());
        CouponValidationResult result = couponService.validateCoupon(userId, request);

        if (result.getIsValid()) {
            return ResponseEntity.ok(ApiResponse.success(result, "Coupon is valid"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(result, result.getErrorMessage()));
        }
    }

    /**
     * Redeem a coupon at checkout
     * POST /api/v1/coupons/redeem
     *
     * Call this when the order is being placed to apply the coupon discount
     */
    @PostMapping("/redeem")
    @Operation(summary = "Redeem coupon",
               description = "Redeem a coupon and apply discount to an order")
    public ResponseEntity<ApiResponse<CouponRedemptionDTO>> redeemCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RedeemCouponRequest request) {
        log.info("User {} redeeming coupon {} for order {}",
                userId, request.getCouponCode(), request.getOrderId());
        CouponRedemptionDTO redemption = couponService.redeemCoupon(userId, request);
        return ResponseEntity.ok(ApiResponse.success(redemption,
                "Coupon applied! You saved RM" + redemption.getDiscountApplied()));
    }

    /**
     * Get user's coupon redemption history
     * GET /api/v1/coupons/my-redemptions
     */
    @GetMapping("/my-redemptions")
    @Operation(summary = "Get my redemptions",
               description = "Get list of coupons I have redeemed")
    public ResponseEntity<ApiResponse<PagedResponse<CouponRedemptionDTO>>> getMyRedemptions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Getting redemptions for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CouponRedemptionDTO> redemptions = couponService.getUserRedemptions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(redemptions));
    }

    /**
     * Complete a coupon redemption (called when order is completed)
     * POST /api/v1/coupons/redemptions/{orderId}/complete
     *
     * This is typically called by the Order Service when order is completed
     */
    @PostMapping("/redemptions/{orderId}/complete")
    @Operation(summary = "Complete redemption",
               description = "Mark coupon redemption as completed when order is finished")
    public ResponseEntity<ApiResponse<Void>> completeRedemption(@PathVariable Long orderId) {
        log.info("Completing redemption for order: {}", orderId);
        couponService.completeRedemption(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Redemption completed"));
    }

    /**
     * Cancel a coupon redemption (called when order is cancelled)
     * POST /api/v1/coupons/redemptions/{orderId}/cancel
     *
     * This is typically called by the Order Service when order is cancelled
     */
    @PostMapping("/redemptions/{orderId}/cancel")
    @Operation(summary = "Cancel redemption",
               description = "Cancel coupon redemption when order is cancelled (restores coupon)")
    public ResponseEntity<ApiResponse<Void>> cancelRedemption(@PathVariable Long orderId) {
        log.info("Cancelling redemption for order: {}", orderId);
        couponService.cancelRedemption(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Redemption cancelled, coupon restored"));
    }
}
