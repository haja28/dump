package com.makanforyou.coupon.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.coupon.dto.*;
import com.makanforyou.coupon.entity.Coupon;
import com.makanforyou.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for coupon management
 * All endpoints require admin authentication
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "Admin - Coupons", description = "Coupon management endpoints for administrators")
public class AdminCouponController {

    private final CouponService couponService;

    /**
     * Create a new coupon
     * POST /api/v1/admin/coupons
     */
    @PostMapping
    @Operation(summary = "Create coupon", description = "Create a new coupon (Admin only)")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
            @RequestHeader("X-User-Id") Long adminId,
            @Valid @RequestBody CreateCouponRequest request) {
        log.info("Admin {} creating coupon: {}", adminId, request.getCode());
        CouponDTO coupon = couponService.createCoupon(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(coupon, "Coupon created successfully"));
    }

    /**
     * Update an existing coupon
     * PUT /api/v1/admin/coupons/{couponId}
     */
    @PutMapping("/{couponId}")
    @Operation(summary = "Update coupon", description = "Update an existing coupon (Admin only)")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {
        log.info("Updating coupon: {}", couponId);
        CouponDTO coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon updated successfully"));
    }

    /**
     * Delete (deactivate) a coupon
     * DELETE /api/v1/admin/coupons/{couponId}
     */
    @DeleteMapping("/{couponId}")
    @Operation(summary = "Delete coupon", description = "Deactivate a coupon (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long couponId) {
        log.info("Deleting coupon: {}", couponId);
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted successfully"));
    }

    /**
     * Get coupon by ID
     * GET /api/v1/admin/coupons/{couponId}
     */
    @GetMapping("/{couponId}")
    @Operation(summary = "Get coupon", description = "Get coupon details by ID (Admin only)")
    public ResponseEntity<ApiResponse<CouponDTO>> getCoupon(@PathVariable Long couponId) {
        log.info("Getting coupon: {}", couponId);
        CouponDTO coupon = couponService.getCouponById(couponId);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    /**
     * Get all coupons with pagination
     * GET /api/v1/admin/coupons
     */
    @GetMapping
    @Operation(summary = "Get all coupons", description = "Get all coupons with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<CouponDTO>>> getAllCoupons(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction) {
        log.info("Getting all coupons - page: {}, size: {}", page, size);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CouponDTO> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * Search coupons
     * GET /api/v1/admin/coupons/search
     */
    @GetMapping("/search")
    @Operation(summary = "Search coupons", description = "Search coupons by code or name (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<CouponDTO>>> searchCoupons(
            @RequestParam String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Searching coupons: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CouponDTO> coupons = couponService.searchCoupons(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * Get coupons by status
     * GET /api/v1/admin/coupons/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get coupons by status", description = "Get coupons filtered by status (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<CouponDTO>>> getCouponsByStatus(
            @PathVariable Coupon.CouponStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Getting coupons by status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CouponDTO> coupons = couponService.getCouponsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    /**
     * Activate a coupon
     * PATCH /api/v1/admin/coupons/{couponId}/activate
     */
    @PatchMapping("/{couponId}/activate")
    @Operation(summary = "Activate coupon", description = "Activate an inactive coupon (Admin only)")
    public ResponseEntity<ApiResponse<CouponDTO>> activateCoupon(@PathVariable Long couponId) {
        log.info("Activating coupon: {}", couponId);
        UpdateCouponRequest request = UpdateCouponRequest.builder()
                .status(Coupon.CouponStatus.ACTIVE)
                .build();
        CouponDTO coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon activated successfully"));
    }

    /**
     * Deactivate a coupon
     * PATCH /api/v1/admin/coupons/{couponId}/deactivate
     */
    @PatchMapping("/{couponId}/deactivate")
    @Operation(summary = "Deactivate coupon", description = "Deactivate an active coupon (Admin only)")
    public ResponseEntity<ApiResponse<CouponDTO>> deactivateCoupon(@PathVariable Long couponId) {
        log.info("Deactivating coupon: {}", couponId);
        UpdateCouponRequest request = UpdateCouponRequest.builder()
                .status(Coupon.CouponStatus.INACTIVE)
                .build();
        CouponDTO coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon deactivated successfully"));
    }
}
