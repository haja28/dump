package com.makanforyou.order.service;

import com.makanforyou.order.entity.Cart;
import com.makanforyou.order.repository.CartItemRepository;
import com.makanforyou.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service to handle cart expiration
 * Carts that are inactive for a configured period will be automatically deleted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartExpirationService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Cart expiration time in hours (default: 24 hours)
     */
    @Value("${cart.expiration.hours:24}")
    private int expirationHours;

    /**
     * Warning time before expiration in hours (default: 2 hours)
     */
    @Value("${cart.expiration.warning-hours:2}")
    private int warningHours;

    /**
     * Run every hour to clean up expired carts
     */
    @Scheduled(cron = "${cart.expiration.cron:0 0 * * * *}") // Every hour at :00
    @Transactional
    public void cleanupExpiredCarts() {
        log.info("Starting expired cart cleanup job...");

        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(expirationHours);

        // First, log how many carts will be deleted
        long expiredCount = cartRepository.countExpiredCarts(expirationThreshold);

        if (expiredCount == 0) {
            log.info("No expired carts found.");
            return;
        }

        log.info("Found {} expired carts (last updated before {})", expiredCount, expirationThreshold);

        // Get expired carts for logging (optional - remove in production for performance)
        List<Cart> expiredCarts = cartRepository.findExpiredCarts(expirationThreshold);
        for (Cart cart : expiredCarts) {
            log.info("Expiring cart ID: {} for user: {} (last updated: {})",
                    cart.getId(), cart.getUserId(), cart.getUpdatedAt());

            // Delete cart items first (cascade should handle this, but being explicit)
            cartItemRepository.deleteByCartId(cart.getId());
        }

        // Delete all expired carts
        int deletedCount = cartRepository.deleteExpiredCarts(expirationThreshold);

        log.info("Expired cart cleanup complete. Deleted {} carts.", deletedCount);
    }

    /**
     * Get expiration time for a cart
     */
    public LocalDateTime getCartExpirationTime(Cart cart) {
        if (cart == null || cart.getUpdatedAt() == null) {
            return null;
        }
        return cart.getUpdatedAt().plusHours(expirationHours);
    }

    /**
     * Get minutes until cart expires
     */
    public long getMinutesUntilExpiry(Cart cart) {
        if (cart == null || cart.getUpdatedAt() == null) {
            return -1;
        }

        LocalDateTime expirationTime = getCartExpirationTime(cart);
        LocalDateTime now = LocalDateTime.now();

        if (expirationTime.isBefore(now)) {
            return 0; // Already expired
        }

        return java.time.Duration.between(now, expirationTime).toMinutes();
    }

    /**
     * Check if cart is about to expire (within warning period)
     */
    public boolean isAboutToExpire(Cart cart) {
        long minutesUntilExpiry = getMinutesUntilExpiry(cart);
        return minutesUntilExpiry >= 0 && minutesUntilExpiry <= (warningHours * 60);
    }

    /**
     * Check if cart is expired
     */
    public boolean isExpired(Cart cart) {
        if (cart == null || cart.getUpdatedAt() == null) {
            return true;
        }

        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(expirationHours);
        return cart.getUpdatedAt().isBefore(expirationThreshold);
    }

    /**
     * Manually expire a specific cart
     */
    @Transactional
    public void expireCart(Long cartId) {
        log.info("Manually expiring cart: {}", cartId);

        cartRepository.findById(cartId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
            log.info("Cart {} expired successfully", cartId);
        });
    }

    /**
     * Get expiration hours setting
     */
    public int getExpirationHours() {
        return expirationHours;
    }
}
