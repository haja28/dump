package com.makanforyou.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem Entity - Items in orders
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(nullable = false, name = "order_id")
    private Long orderId;

    @Column(nullable = false, name = "item_id")
    private Long itemId;

    @Column(nullable = false, name = "item_quantity")
    private Integer itemQuantity;

    @Column(nullable = false, precision = 10, scale = 2, name = "item_unit_price")
    private BigDecimal itemUnitPrice;

    @Column(nullable = false, precision = 10, scale = 2, name = "item_total")
    private BigDecimal itemTotal;

    @Column(length = 500, name = "special_requests")
    private String specialRequests;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
