package com.makanforyou.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Entity - Customer orders
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_kitchen_id", columnList = "kitchen_id"),
        @Index(name = "idx_order_date", columnList = "order_date"),
        @Index(name = "idx_order_status", columnList = "order_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "kitchen_id")
    private Long kitchenId;

    @Column(nullable = false, precision = 10, scale = 2, name = "order_total")
    private BigDecimal orderTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "order_status")
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "confirmation_by_kitchen")
    @Builder.Default
    private Boolean confirmationByKitchen = false;

    @Column(name = "confirmation_timestamp")
    private LocalDateTime confirmationTimestamp;

    @Column(nullable = false, length = 255, name = "delivery_address")
    private String deliveryAddress;

    @Column(length = 50, name = "delivery_city")
    private String deliveryCity;

    @Column(length = 50, name = "delivery_state")
    private String deliveryState;

    @Column(length = 10, name = "delivery_postal_code")
    private String deliveryPostalCode;

    @Column(length = 500, name = "special_instructions")
    private String specialInstructions;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Order status enum
     */
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PREPARING,
        READY,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED
    }
}
