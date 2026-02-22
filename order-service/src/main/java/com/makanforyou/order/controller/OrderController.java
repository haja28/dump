package com.makanforyou.order.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.order.dto.*;
import com.makanforyou.order.entity.Order;
import com.makanforyou.order.service.OrderService;
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
 * Controller for order endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create order
     * POST /api/v1/orders
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Customer creates new order")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    /**
     * Create order from cart
     * POST /api/v1/orders/from-cart
     */
    @PostMapping("/from-cart")
    @Operation(summary = "Create order from cart", description = "Customer creates order from their shopping cart")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrderFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderFromCartRequest request) {
        log.info("Creating order from cart for user: {}", userId);
        OrderDTO order = orderService.createOrderFromCart(
                userId,
                request.getDeliveryAddress(),
                request.getDeliveryCity(),
                request.getDeliveryState(),
                request.getDeliveryPostalCode(),
                request.getSpecialInstructions()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created from cart successfully"));
    }

    /**
     * Get current user's orders
     * GET /api/v1/orders/my-orders
     */
    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders", description = "Retrieve current user's orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDTO> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get my kitchen's orders (current kitchen owner)
     * GET /api/v1/orders/kitchen/my-orders
     */
    @GetMapping("/kitchen/my-orders")
    @Operation(summary = "Get my kitchen orders", description = "Retrieve orders for current user's kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getMyKitchenOrders(
            @RequestHeader("X-Kitchen-Id") Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching orders for my kitchen: {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDTO> response = orderService.getKitchenOrders(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get kitchen's orders
     * GET /api/v1/orders/kitchen/{kitchenId}
     */
    @GetMapping("/kitchen/{kitchenId}")
    @Operation(summary = "Get kitchen orders", description = "Retrieve all orders for a kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getKitchenOrders(
            @PathVariable Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching orders for kitchen: {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDTO> response = orderService.getKitchenOrders(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get kitchen's pending orders
     * GET /api/v1/orders/kitchen/{kitchenId}/pending
     */
    @GetMapping("/kitchen/{kitchenId}/pending")
    @Operation(summary = "Get pending orders", description = "Retrieve pending orders for a kitchen")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getKitchenPendingOrders(
            @PathVariable Long kitchenId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching pending orders for kitchen: {}", kitchenId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDTO> response = orderService.getKitchenPendingOrders(kitchenId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get order by ID
     * GET /api/v1/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Retrieve order details by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long orderId) {
        log.info("Getting order: {}", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Accept order
     * PATCH /api/v1/orders/{orderId}/accept
     */
    @PatchMapping("/{orderId}/accept")
    @Operation(summary = "Accept order", description = "Kitchen accepts order")
    public ResponseEntity<ApiResponse<OrderDTO>> acceptOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-Kitchen-Id") Long kitchenId) {
        log.info("Accepting order: {}", orderId);
        OrderDTO order = orderService.acceptOrder(orderId, kitchenId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order accepted successfully"));
    }

    /**
     * Update order status
     * PATCH /api/v1/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        log.info("Updating order status to: {}", status);
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated successfully"));
    }

    /**
     * Cancel order
     * PATCH /api/v1/orders/{orderId}/cancel
     */
    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel order")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order: {}", orderId);
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }
}
