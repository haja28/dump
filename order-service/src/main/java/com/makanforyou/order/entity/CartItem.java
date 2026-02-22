package com.makanforyou.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItem Entity - Individual items in a shopping cart
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart_id", columnList = "cart_id"),
        @Index(name = "idx_cart_item_item_id", columnList = "item_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false, name = "item_id")
    private Long itemId;

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "item_description", length = 500)
    private String itemDescription;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2, name = "unit_price")
    private BigDecimal unitPrice;

    /**
     * Original price when item was added to cart (for price change detection)
     */
    @Column(precision = 10, scale = 2, name = "original_price")
    private BigDecimal originalPrice;

    /**
     * Current price from menu (updated on refresh)
     */
    @Column(precision = 10, scale = 2, name = "current_menu_price")
    private BigDecimal currentMenuPrice;

    /**
     * Indicates if the price has changed since item was added
     */
    @Column(name = "price_changed")
    @Builder.Default
    private Boolean priceChanged = false;

    /**
     * Stock status - true if item is in stock
     */
    @Column(name = "in_stock")
    @Builder.Default
    private Boolean inStock = true;

    /**
     * Available stock quantity (null means unlimited)
     */
    @Column(name = "available_stock")
    private Integer availableStock;

    @Column(length = 500, name = "special_requests")
    private String specialRequests;

    /**
     * When the item was added to cart
     */
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        if (originalPrice == null) {
            originalPrice = unitPrice;
        }
    }

    /**
     * Calculate item total (unit price * quantity)
     */
    public BigDecimal getItemTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Check if price has changed from original
     */
    public boolean hasPriceChanged() {
        if (originalPrice == null || unitPrice == null) {
            return false;
        }
        return originalPrice.compareTo(unitPrice) != 0;
    }

    /**
     * Get price difference (positive = price increased, negative = price decreased)
     */
    public BigDecimal getPriceDifference() {
        if (originalPrice == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.subtract(originalPrice);
    }
}
