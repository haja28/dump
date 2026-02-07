package com.makanforyou.menu.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.menu.dto.MenuItemDTO;
import com.makanforyou.menu.dto.MenuItemRequest;
import com.makanforyou.menu.dto.MenuSearchFilter;
import com.makanforyou.menu.service.MenuItemService;
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller for menu item endpoints with search and filtering
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "Menu item management and search endpoints")
public class MenuItemController {

    private final MenuItemService menuItemService;

    /**
     * Create menu item (kitchen owner only)
     * POST /api/v1/menu-items
     */
    @PostMapping
    @Operation(summary = "Create menu item", description = "Kitchen owner creates new menu item")
    public ResponseEntity<ApiResponse<MenuItemDTO>> createMenuItem(
            @RequestHeader("X-Kitchen-Id") Long kitchenId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("Creating menu item for kitchen: {}", kitchenId);
        MenuItemDTO item = menuItemService.createMenuItem(kitchenId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item, "Menu item created successfully"));
    }

    /**
     * Get menu item by ID
     * GET /api/v1/menu-items/{itemId}
     */
    @GetMapping("/{itemId}")
    @Operation(summary = "Get menu item", description = "Retrieve menu item details")
    public ResponseEntity<ApiResponse<MenuItemDTO>> getMenuItem(@PathVariable Long itemId) {
        log.info("Getting menu item: {}", itemId);
        MenuItemDTO item = menuItemService.getMenuItemById(itemId);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * Get all menu items for a kitchen
     * GET /api/v1/menu-items/kitchen/{kitchenId}
     */
    @GetMapping("/kitchen/{kitchenId}")
    @Operation(summary = "Get kitchen menu", description = "Retrieve all menu items for a kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<MenuItemDTO>>> getKitchenMenu(
            @PathVariable Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching menu for kitchen: {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MenuItemDTO> response = menuItemService.getKitchenMenuItems(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Advanced search with filtering
     * GET /api/v1/menu-items/search
     *
     * Example: GET /api/v1/menu-items/search?query=biryani&kitchenId=123&labels=halal,spicy&minPrice=5&maxPrice=15&veg=false&sort=rating_desc
     */
    @GetMapping("/search")
    @Operation(summary = "Search menu items", description = "Advanced search with multiple filters")
    public ResponseEntity<ApiResponse<PagedResponse<MenuItemDTO>>> searchMenuItems(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "kitchenId", required = false) Long kitchenId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "veg", required = false) Boolean veg,
            @RequestParam(name = "halal", required = false) Boolean halal,
            @RequestParam(name = "minSpicyLevel", required = false) Integer minSpicyLevel,
            @RequestParam(name = "maxSpicyLevel", required = false) Integer maxSpicyLevel,
            @RequestParam(name = "labels", required = false) String labels,
            @RequestParam(name = "sort", defaultValue = "rating_desc") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        log.info("Searching menu items with query: {}", query);

        // Parse labels
        Set<String> labelSet = new HashSet<>();
        if (labels != null && !labels.isEmpty()) {
            String[] labelArray = labels.split(",");
            for (String label : labelArray) {
                labelSet.add(label.trim());
            }
        }

        MenuSearchFilter filter = MenuSearchFilter.builder()
                .query(query)
                .kitchenId(kitchenId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isVeg(veg)
                .isHalal(halal)
                .minSpicyLevel(minSpicyLevel)
                .maxSpicyLevel(maxSpicyLevel)
                .labels(labelSet)
                .sortBy(sort)
                .isActive(true)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MenuItemDTO> response = menuItemService.searchMenuItems(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update menu item (kitchen owner only)
     * PUT /api/v1/menu-items/{itemId}
     */
    @PutMapping("/{itemId}")
    @Operation(summary = "Update menu item", description = "Kitchen owner updates menu item")
    public ResponseEntity<ApiResponse<MenuItemDTO>> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("Updating menu item: {}", itemId);
        MenuItemDTO item = menuItemService.updateMenuItem(itemId, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Menu item updated successfully"));
    }

    /**
     * Deactivate menu item
     * PATCH /api/v1/menu-items/{itemId}/deactivate
     */
    @PatchMapping("/{itemId}/deactivate")
    @Operation(summary = "Deactivate menu item", description = "Deactivate menu item")
    public ResponseEntity<ApiResponse<MenuItemDTO>> deactivateMenuItem(@PathVariable Long itemId) {
        log.info("Deactivating menu item: {}", itemId);
        MenuItemDTO item = menuItemService.deactivateMenuItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(item, "Menu item deactivated successfully"));
    }
}
