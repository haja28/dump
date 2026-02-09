package com.makanforyou.order.service;

import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.order.dto.*;
import com.makanforyou.order.entity.Cart;
import com.makanforyou.order.entity.CartItem;
import com.makanforyou.order.entity.Order;
import com.makanforyou.order.entity.OrderItem;
import com.makanforyou.order.repository.OrderItemRepository;
import com.makanforyou.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for order operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Lazy
    private final CartService cartService;

    /**
     * Create new order from direct request
     */
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            // Note: In production, fetch item price from menu service
            total = total.add(BigDecimal.ZERO); // Placeholder
        }

        Order order = Order.builder()
                .userId(userId)
                .kitchenId(request.getKitchenId())
                .orderTotal(total)
                .orderStatus(Order.OrderStatus.PENDING)
                .confirmationByKitchen(false)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryCity(request.getDeliveryCity())
                .deliveryState(request.getDeliveryState())
                .deliveryPostalCode(request.getDeliveryPostalCode())
                .specialInstructions(request.getSpecialInstructions())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        // Create order items
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .itemId(itemRequest.getItemId())
                    .itemQuantity(itemRequest.getQuantity())
                    .itemUnitPrice(BigDecimal.ZERO) // Fetch from menu service
                    .itemTotal(BigDecimal.ZERO) // Calculate
                    .specialRequests(itemRequest.getSpecialRequests())
                    .build();
            orderItemRepository.save(orderItem);
        }

        log.info("Order created with ID: {}", order.getId());
        return mapToDTO(order);
    }

    /**
     * Create order from user's cart
     */
    public OrderDTO createOrderFromCart(Long userId, String deliveryAddress, String deliveryCity,
                                         String deliveryState, String deliveryPostalCode,
                                         String specialInstructions) {
        log.info("Creating order from cart for user: {}", userId);

        // Get cart and validate
        Cart cart = cartService.getCartForCheckout(userId);

        // Calculate total from cart
        BigDecimal orderTotal = cart.getTotal();

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .kitchenId(cart.getKitchenId())
                .orderTotal(orderTotal)
                .orderStatus(Order.OrderStatus.PENDING)
                .confirmationByKitchen(false)
                .deliveryAddress(deliveryAddress)
                .deliveryCity(deliveryCity)
                .deliveryState(deliveryState)
                .deliveryPostalCode(deliveryPostalCode)
                .specialInstructions(specialInstructions)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .itemId(cartItem.getItemId())
                    .itemQuantity(cartItem.getQuantity())
                    .itemUnitPrice(cartItem.getUnitPrice())
                    .itemTotal(cartItem.getItemTotal())
                    .specialRequests(cartItem.getSpecialRequests())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Clear the cart after successful order creation
        cartService.clearCart(userId);

        log.info("Order created from cart with ID: {}", order.getId());
        return mapToDTO(order);
    }

    /**
     * Get order by ID
     */
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND", "Order not found"));
        return mapToDTO(order);
    }

    /**
     * Get order by ID and user ID (user can only see their own orders)
     */
    public OrderDTO getOrderByIdForUser(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND", "Order not found"));
        return mapToDTO(order);
    }

    /**
     * Get user's orders
     */
    public PagedResponse<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        log.info("Fetching orders for user: {}", userId);
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Get kitchen's orders
     */
    public PagedResponse<OrderDTO> getKitchenOrders(Long kitchenId, Pageable pageable) {
        log.info("Fetching orders for kitchen: {}", kitchenId);
        Page<Order> page = orderRepository.findByKitchenId(kitchenId, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Get kitchen's pending orders
     */
    public PagedResponse<OrderDTO> getKitchenPendingOrders(Long kitchenId, Pageable pageable) {
        log.info("Fetching pending orders for kitchen: {}", kitchenId);
        Page<Order> page = orderRepository.findByKitchenIdAndOrderStatus(
                kitchenId, Order.OrderStatus.PENDING, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Accept order (kitchen confirms)
     */
    public OrderDTO acceptOrder(Long orderId, Long kitchenId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.getKitchenId().equals(kitchenId)) {
            throw new ApplicationException("UNAUTHORIZED", "Kitchen cannot accept this order");
        }

        order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        order.setConfirmationByKitchen(true);
        order.setConfirmationTimestamp(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        log.info("Order accepted: {}", orderId);
        return mapToDTO(order);
    }

    /**
     * Update order status
     */
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND", "Order not found"));

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        log.info("Order status updated to: {} for order: {}", newStatus, orderId);
        return mapToDTO(order);
    }

    /**
     * Cancel order
     */
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException("ORDER_NOT_FOUND", "Order not found"));

        if (order.getOrderStatus().equals(Order.OrderStatus.DELIVERED) ||
            order.getOrderStatus().equals(Order.OrderStatus.OUT_FOR_DELIVERY)) {
            throw new ApplicationException("CANNOT_CANCEL",
                    "Cannot cancel order in " + order.getOrderStatus() + " status");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        log.info("Order cancelled: {}", orderId);
        return mapToDTO(order);
    }

    /**
     * Map Order entity to DTO
     */
    private OrderDTO mapToDTO(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .kitchenId(order.getKitchenId())
                .orderTotal(order.getOrderTotal())
                .orderStatus(order.getOrderStatus())
                .confirmationByKitchen(order.getConfirmationByKitchen())
                .confirmationTimestamp(order.getConfirmationTimestamp())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryCity(order.getDeliveryCity())
                .deliveryState(order.getDeliveryState())
                .deliveryPostalCode(order.getDeliveryPostalCode())
                .specialInstructions(order.getSpecialInstructions())
                .items(items.stream().map(this::mapOrderItemToDTO).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Map OrderItem to DTO
     */
    private OrderItemDTO mapOrderItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .itemId(item.getItemId())
                .itemQuantity(item.getItemQuantity())
                .itemUnitPrice(item.getItemUnitPrice())
                .itemTotal(item.getItemTotal())
                .specialRequests(item.getSpecialRequests())
                .build();
    }

    /**
     * Build paged response
     */
    private PagedResponse<OrderDTO> buildPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderDTO>builder()
                .content(page.getContent().stream().map(this::mapToDTO).toList())
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
