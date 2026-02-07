package com.makanforyou.order.repository;

import com.makanforyou.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByKitchenId(Long kitchenId, Pageable pageable);
    Page<Order> findByKitchenIdAndOrderStatus(Long kitchenId, Order.OrderStatus status, Pageable pageable);
}
