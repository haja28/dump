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
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Entity - Shopping cart for customers
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @Column(nullable = false, name = "user_id", unique = true)
    private Long userId;

    @Column(name = "kitchen_id")
    private Long kitchenId;

    @Column(name = "kitchen_name", length = 100)
    private String kitchenName;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_description", length = 255)
    private String couponDescription;

    @Column(precision = 10, scale = 2, name = "discount_amount")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, name = "delivery_fee")
    @Builder.Default
    private BigDecimal deliveryFee = new BigDecimal("3.00");

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate subtotal (sum of all item totals)
     */
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total (subtotal + delivery fee - discount)
     */
    public BigDecimal getTotal() {
        return getSubtotal()
                .add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    /**
     * Get total item count
     */
    public int getItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /**
     * Clear all items
     */
    public void clearItems() {
        items.clear();
    }
}
