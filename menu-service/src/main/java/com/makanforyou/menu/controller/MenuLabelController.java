package com.makanforyou.menu.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.menu.dto.MenuLabelDTO;
import com.makanforyou.menu.service.MenuLabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for menu label endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menu-labels")
@RequiredArgsConstructor
@Tag(name = "Menu Labels", description = "Menu label management endpoints")
public class MenuLabelController {

    private final MenuLabelService labelService;

    /**
     * Create new label (Admin only)
     * POST /api/v1/menu-labels
     */
    @PostMapping
    @Operation(summary = "Create menu label", description = "Admin creates new menu label")
    public ResponseEntity<ApiResponse<MenuLabelDTO>> createLabel(
            @RequestParam(name = "name") @NotBlank String name,
            @RequestParam(name = "description", required = false) String description) {
        log.info("Creating label: {}", name);
        MenuLabelDTO label = labelService.createLabel(name, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(label, "Label created successfully"));
    }

    /**
     * Get all labels
     * GET /api/v1/menu-labels
     */
    @GetMapping
    @Operation(summary = "Get all labels", description = "Retrieve all active menu labels")
    public ResponseEntity<ApiResponse<List<MenuLabelDTO>>> getAllLabels() {
        log.info("Fetching all labels");
        List<MenuLabelDTO> labels = labelService.getAllLabels();
        return ResponseEntity.ok(ApiResponse.success(labels));
    }

    /**
     * Get label by ID
     * GET /api/v1/menu-labels/{labelId}
     */
    @GetMapping("/{labelId}")
    @Operation(summary = "Get label", description = "Retrieve label by ID")
    public ResponseEntity<ApiResponse<MenuLabelDTO>> getLabel(@PathVariable Long labelId) {
        log.info("Getting label: {}", labelId);
        MenuLabelDTO label = labelService.getLabelById(labelId);
        return ResponseEntity.ok(ApiResponse.success(label));
    }

    /**
     * Update label (Admin only)
     * PUT /api/v1/menu-labels/{labelId}
     */
    @PutMapping("/{labelId}")
    @Operation(summary = "Update label", description = "Admin updates menu label")
    public ResponseEntity<ApiResponse<MenuLabelDTO>> updateLabel(
            @PathVariable Long labelId,
            @RequestParam(name = "name") @NotBlank String name,
            @RequestParam(name = "description", required = false) String description) {
        log.info("Updating label: {}", labelId);
        MenuLabelDTO label = labelService.updateLabel(labelId, name, description);
        return ResponseEntity.ok(ApiResponse.success(label, "Label updated successfully"));
    }

    /**
     * Deactivate label (Admin only)
     * PATCH /api/v1/menu-labels/{labelId}/deactivate
     */
    @PatchMapping("/{labelId}/deactivate")
    @Operation(summary = "Deactivate label", description = "Admin deactivates menu label")
    public ResponseEntity<ApiResponse<MenuLabelDTO>> deactivateLabel(@PathVariable Long labelId) {
        log.info("Deactivating label: {}", labelId);
        MenuLabelDTO label = labelService.deactivateLabel(labelId);
        return ResponseEntity.ok(ApiResponse.success(label, "Label deactivated successfully"));
    }
}
