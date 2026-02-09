package com.makanforyou.order.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.order.dto.*;
import com.makanforyou.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for cart endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart
     * GET /api/v1/cart
     */
    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve current user's shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Getting cart for user: {}", userId);
        CartDTO cart = cartService.getCart(userId);
        String message = cart.getItems().isEmpty() ? "Cart is empty" : "Cart retrieved successfully";
        return ResponseEntity.ok(ApiResponse.success(cart, message));
    }

    /**
     * Add item to cart
     * POST /api/v1/cart/items
     */
    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a menu item to the shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Adding item {} to cart for user {}", request.getItemId(), userId);
        CartDTO cart = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Item added to cart"));
    }

    /**
     * Update cart item
     * PUT /api/v1/cart/items/{cartItemId}
     */
    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item", description = "Update quantity or special requests for a cart item")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", cartItemId, userId);
        CartDTO cart = cartService.updateItem(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated"));
    }

    /**
     * Remove item from cart
     * DELETE /api/v1/cart/items/{cartItemId}
     */
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove cart item", description = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, userId);
        CartDTO cart = cartService.removeItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
    }

    /**
     * Clear cart
     * DELETE /api/v1/cart
     */
    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Clearing cart for user {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
    }

    /**
     * Apply coupon to cart
     * POST /api/v1/cart/coupon
     */
    @PostMapping("/coupon")
    @Operation(summary = "Apply coupon", description = "Apply a coupon code to the cart")
    public ResponseEntity<ApiResponse<CartDTO>> applyCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ApplyCouponRequest request) {
        log.info("Applying coupon {} for user {}", request.getCouponCode(), userId);
        CartDTO cart = cartService.applyCoupon(userId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Coupon applied successfully"));
    }

    /**
     * Remove coupon from cart
     * DELETE /api/v1/cart/coupon
     */
    @DeleteMapping("/coupon")
    @Operation(summary = "Remove coupon", description = "Remove the applied coupon from the cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeCoupon(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Removing coupon for user {}", userId);
        CartDTO cart = cartService.removeCoupon(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Coupon removed successfully"));
    }

    /**
     * Refresh cart - updates stock availability and prices
     * POST /api/v1/cart/refresh
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh cart", description = "Update cart with latest stock availability and prices from menu")
    public ResponseEntity<ApiResponse<CartDTO>> refreshCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Refreshing cart for user {}", userId);
        CartDTO cart = cartService.refreshCart(userId);

        String message = "Cart refreshed successfully";
        if (cart.getHasPriceChanges()) {
            message += ". Note: Some prices have changed.";
        }
        if (cart.getHasStockIssues()) {
            message += ". Warning: Some items have stock issues.";
        }

        return ResponseEntity.ok(ApiResponse.success(cart, message));
    }

    /**
     * Validate cart for checkout
     * POST /api/v1/cart/validate
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate cart", description = "Validate cart is ready for checkout (checks stock and availability)")
    public ResponseEntity<ApiResponse<CartDTO>> validateCart(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Validating cart for checkout for user {}", userId);
        CartDTO cart = cartService.validateCartForCheckout(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart is valid and ready for checkout"));
    }
}
