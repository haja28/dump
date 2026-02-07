package com.makanforyou.kitchen.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.kitchen.dto.KitchenDTO;
import com.makanforyou.kitchen.dto.KitchenRegistrationRequest;
import com.makanforyou.kitchen.service.KitchenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for kitchen endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kitchens")
@RequiredArgsConstructor
@Tag(name = "Kitchens", description = "Kitchen management endpoints")
public class KitchenController {

    private final KitchenService kitchenService;

    /**
     * Register new kitchen
     * POST /api/v1/kitchens/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register new kitchen", description = "Home chef registers their kitchen")
    public ResponseEntity<ApiResponse<KitchenDTO>> registerKitchen(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody KitchenRegistrationRequest request) {
        log.info("Kitchen registration request for user: {}", userId);
        KitchenDTO kitchen = kitchenService.registerKitchen(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(kitchen, "Kitchen registered successfully"));
    }

    /**
     * Get kitchen by ID
     * GET /api/v1/kitchens/{kitchenId}
     */
    @GetMapping("/{kitchenId}")
    @Operation(summary = "Get kitchen details", description = "Retrieve kitchen information by ID")
    public ResponseEntity<ApiResponse<KitchenDTO>> getKitchen(@PathVariable("kitchenId") Long kitchenId) {
        log.info("Getting kitchen: {}", kitchenId);
        KitchenDTO kitchen = kitchenService.getKitchenById(kitchenId);
        return ResponseEntity.ok(ApiResponse.success(kitchen));
    }

    /**
     * Get my kitchen (current user)
     * GET /api/v1/kitchens/my-kitchen
     */
    @GetMapping("/my-kitchen")
    @Operation(summary = "Get my kitchen", description = "Retrieve current user's kitchen")
    public ResponseEntity<ApiResponse<KitchenDTO>> getMyKitchen(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Getting kitchen for user: {}", userId);
        KitchenDTO kitchen = kitchenService.getKitchenByOwnerUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(kitchen));
    }

    /**
     * Get approved kitchens (public)
     * GET /api/v1/kitchens?approved=true
     */
    @GetMapping
    @Operation(summary = "Get approved kitchens", description = "Retrieve list of approved and active kitchens")
    public ResponseEntity<ApiResponse<PagedResponse<KitchenDTO>>> getApprovedKitchens(
            @RequestParam(name = "approved", defaultValue = "true") boolean approved,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Getting kitchens with approved={}", approved);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KitchenDTO> response = approved ?
                kitchenService.getApprovedKitchens(pageable) :
                kitchenService.getAllKitchens(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search kitchens
     * GET /api/v1/kitchens/search?query=biryani
     */
    @GetMapping("/search")
    @Operation(summary = "Search kitchens", description = "Search kitchens by name or description")
    public ResponseEntity<ApiResponse<PagedResponse<KitchenDTO>>> searchKitchens(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Searching kitchens with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KitchenDTO> response = kitchenService.searchKitchens(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get kitchens by city
     * GET /api/v1/kitchens/by-city/{city}
     */
    @GetMapping("/by-city/{city}")
    @Operation(summary = "Get kitchens by city", description = "Retrieve kitchens in a specific city")
    public ResponseEntity<ApiResponse<PagedResponse<KitchenDTO>>> getKitchensByCity(
            @PathVariable String city,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Getting kitchens in city: {}", city);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KitchenDTO> response = kitchenService.getKitchensByCity(city, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update kitchen (kitchen owner only)
     * PUT /api/v1/kitchens/{kitchenId}
     */
    @PutMapping("/{kitchenId}")
    @Operation(summary = "Update kitchen", description = "Update kitchen profile details")
    public ResponseEntity<ApiResponse<KitchenDTO>> updateKitchen(
            @PathVariable("kitchenId") Long kitchenId,
            @Valid @RequestBody KitchenRegistrationRequest request) {
        log.info("Updating kitchen: {}", kitchenId);
        KitchenDTO kitchen = kitchenService.updateKitchen(kitchenId, request);
        return ResponseEntity.ok(ApiResponse.success(kitchen, "Kitchen updated successfully"));
    }

    /**
     * Approve kitchen (admin only)
     * PATCH /api/v1/kitchens/{kitchenId}/approve
     */
    @PatchMapping("/{kitchenId}/approve")
    @Operation(summary = "Approve kitchen", description = "Admin approves pending kitchen")
    public ResponseEntity<ApiResponse<KitchenDTO>> approveKitchen(@PathVariable("kitchenId") Long kitchenId) {
        log.info("Approving kitchen: {}", kitchenId);
        KitchenDTO kitchen = kitchenService.approveKitchen(kitchenId);
        return ResponseEntity.ok(ApiResponse.success(kitchen, "Kitchen approved successfully"));
    }

    /**
     * Reject kitchen (admin only)
     * PATCH /api/v1/kitchens/{kitchenId}/reject
     */
    @PatchMapping("/{kitchenId}/reject")
    @Operation(summary = "Reject kitchen", description = "Admin rejects pending kitchen")
    public ResponseEntity<ApiResponse<KitchenDTO>> rejectKitchen(@PathVariable("kitchenId") Long kitchenId) {
        log.info("Rejecting kitchen: {}", kitchenId);
        KitchenDTO kitchen = kitchenService.rejectKitchen(kitchenId);
        return ResponseEntity.ok(ApiResponse.success(kitchen, "Kitchen rejected successfully"));
    }

    /**
     * Deactivate kitchen
     * PATCH /api/v1/kitchens/{kitchenId}/deactivate
     */
    @PatchMapping("/{kitchenId}/deactivate")
    @Operation(summary = "Deactivate kitchen", description = "Deactivate kitchen profile")
    public ResponseEntity<ApiResponse<KitchenDTO>> deactivateKitchen(@PathVariable("kitchenId") Long kitchenId) {
        log.info("Deactivating kitchen: {}", kitchenId);
        KitchenDTO kitchen = kitchenService.deactivateKitchen(kitchenId);
        return ResponseEntity.ok(ApiResponse.success(kitchen, "Kitchen deactivated successfully"));
    }
}
