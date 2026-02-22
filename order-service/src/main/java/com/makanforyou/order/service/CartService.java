package com.makanforyou.order.service;

import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.order.dto.*;
import com.makanforyou.order.entity.Cart;
import com.makanforyou.order.entity.CartItem;
import com.makanforyou.order.repository.CartItemRepository;
import com.makanforyou.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for cart operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuServiceClient menuServiceClient;
    private final CartExpirationService cartExpirationService;

    /**
     * Get cart for user
     */
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.info("Getting cart for user: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            // Return empty cart
            return CartDTO.builder()
                    .cartId(null)
                    .userId(userId)
                    .kitchenId(null)
                    .kitchenName(null)
                    .items(new ArrayList<>())
                    .subtotal(BigDecimal.ZERO)
                    .deliveryFee(BigDecimal.ZERO)
                    .discount(BigDecimal.ZERO)
                    .couponCode(null)
                    .total(BigDecimal.ZERO)
                    .itemCount(0)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();
        }

        return mapToDTO(cartOpt.get());
    }

    /**
     * Add item to cart with stock validation
     */
    public CartDTO addItem(Long userId, AddToCartRequest request) {
        log.info("Adding item {} to cart for user {}", request.getItemId(), userId);

        // Fetch item details from Menu Service
        MenuItemDTO menuItem = menuServiceClient.getMenuItem(request.getItemId())
                .orElseThrow(() -> new ApplicationException("ITEM_NOT_FOUND",
                        "Menu item with ID " + request.getItemId() + " does not exist"));

        // Validate item is available
        if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
            throw new ApplicationException("ITEM_UNAVAILABLE",
                    "'" + menuItem.getItemName() + "' is currently not available for ordering");
        }

        // Validate stock
        if (!menuItem.hasStock(request.getQuantity())) {
            String stockMessage = menuItem.getStockQuantity() != null && menuItem.getStockQuantity() > 0
                    ? "Only " + menuItem.getStockQuantity() + " available"
                    : "Out of stock";
            throw new ApplicationException("INSUFFICIENT_STOCK",
                    "'" + menuItem.getItemName() + "' - " + stockMessage);
        }

        // Validate max order quantity
        if (menuItem.exceedsMaxOrder(request.getQuantity())) {
            throw new ApplicationException("MAX_QUANTITY_EXCEEDED",
                    "Maximum order quantity for '" + menuItem.getItemName() + "' is " + menuItem.getMaxOrderQuantity());
        }

        Long kitchenId = menuItem.getKitchenId();
        String kitchenName = menuItem.getKitchenName();

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .kitchenId(kitchenId)
                            .kitchenName(kitchenName)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if cart is from a different kitchen
        if (cart.getKitchenId() != null && !cart.getKitchenId().equals(kitchenId)) {
            throw new ApplicationException("DIFFERENT_KITCHEN",
                    "Cannot add items from different kitchen. Please clear your cart first.");
        }

        // Update kitchen info if not set
        if (cart.getKitchenId() == null) {
            cart.setKitchenId(kitchenId);
            cart.setKitchenName(kitchenName);
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getItemId().equals(request.getItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            // Validate total quantity against stock
            if (!menuItem.hasStock(newQuantity)) {
                throw new ApplicationException("INSUFFICIENT_STOCK",
                        "Cannot add " + request.getQuantity() + " more. Total would exceed available stock.");
            }

            // Validate total against max order
            if (menuItem.exceedsMaxOrder(newQuantity)) {
                throw new ApplicationException("MAX_QUANTITY_EXCEEDED",
                        "Maximum order quantity for '" + menuItem.getItemName() + "' is " + menuItem.getMaxOrderQuantity());
            }

            item.setQuantity(newQuantity);
            item.setUnitPrice(menuItem.getUnitPrice());
            item.setCurrentMenuPrice(menuItem.getUnitPrice());
            item.setInStock(true);
            item.setAvailableStock(menuItem.getStockQuantity());

            if (request.getSpecialRequests() != null) {
                item.setSpecialRequests(request.getSpecialRequests());
            }
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .itemId(request.getItemId())
                    .itemName(menuItem.getItemName())
                    .itemDescription(menuItem.getItemDescription())
                    .imageUrl(menuItem.getImageUrl())
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getUnitPrice())
                    .originalPrice(menuItem.getUnitPrice())
                    .currentMenuPrice(menuItem.getUnitPrice())
                    .priceChanged(false)
                    .inStock(true)
                    .availableStock(menuItem.getStockQuantity())
                    .specialRequests(request.getSpecialRequests())
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart = cartRepository.save(cart);
        log.info("Item added to cart. Cart ID: {}", cart.getId());

        return mapToDTO(cart);
    }

    /**
     * Update cart item
     */
    public CartDTO updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApplicationException("UNAUTHORIZED", "Cart item does not belong to your cart");
        }

        cartItem.setQuantity(request.getQuantity());
        if (request.getSpecialRequests() != null) {
            cartItem.setSpecialRequests(request.getSpecialRequests());
        }

        cartItemRepository.save(cartItem);
        cart = cartRepository.save(cart);

        log.info("Cart item updated. Cart ID: {}", cart.getId());
        return mapToDTO(cart);
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItem(Long userId, Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApplicationException("UNAUTHORIZED", "Cart item does not belong to your cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // If cart is empty, clear kitchen info
        if (cart.getItems().isEmpty()) {
            cart.setKitchenId(null);
            cart.setKitchenName(null);
            cart.setCouponCode(null);
            cart.setCouponDescription(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
        }

        cart = cartRepository.save(cart);
        log.info("Cart item removed. Cart ID: {}", cart.getId());

        return mapToDTO(cart);
    }

    /**
     * Clear cart
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
            log.info("Cart cleared for user {}", userId);
        }
    }

    /**
     * Apply coupon to cart
     */
    public CartDTO applyCoupon(Long userId, ApplyCouponRequest request) {
        log.info("Applying coupon {} to cart for user {}", request.getCouponCode(), userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ApplicationException("CART_EMPTY", "Cannot apply coupon to empty cart");
        }

        // TODO: Validate coupon with Coupon Service
        // CouponDTO coupon = couponServiceClient.validateCoupon(request.getCouponCode());

        // For now, mock coupon validation
        String couponCode = request.getCouponCode().toUpperCase();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String couponDescription = "";

        if ("SAVE10".equals(couponCode)) {
            // 10% off
            discountAmount = cart.getSubtotal().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            couponDescription = "10% off your order";
        } else if ("SAVE5".equals(couponCode)) {
            // $5 off
            discountAmount = new BigDecimal("5.00");
            couponDescription = "$5 off your order";
        } else if ("FREESHIP".equals(couponCode)) {
            // Free delivery
            discountAmount = cart.getDeliveryFee();
            couponDescription = "Free delivery";
        } else {
            throw new ApplicationException("INVALID_COUPON", "Coupon code is not valid or has expired");
        }

        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(cart.getSubtotal()) > 0) {
            discountAmount = cart.getSubtotal();
        }

        cart.setCouponCode(couponCode);
        cart.setCouponDescription(couponDescription);
        cart.setDiscountAmount(discountAmount);

        cart = cartRepository.save(cart);
        log.info("Coupon applied. Discount: {}", discountAmount);

        return mapToDTO(cart);
    }

    /**
     * Remove coupon from cart
     */
    public CartDTO removeCoupon(Long userId) {
        log.info("Removing coupon from cart for user {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        cart.setCouponCode(null);
        cart.setCouponDescription(null);
        cart.setDiscountAmount(BigDecimal.ZERO);

        cart = cartRepository.save(cart);
        log.info("Coupon removed from cart");

        return mapToDTO(cart);
    }

    /**
     * Get cart for checkout (validates cart is ready for order)
     */
    @Transactional(readOnly = true)
    public Cart getCartForCheckout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ApplicationException("CART_EMPTY", "Cart is empty");
        }

        if (cart.getKitchenId() == null) {
            throw new ApplicationException("INVALID_CART", "Cart has no kitchen assigned");
        }

        return cart;
    }

    /**
     * Map Cart entity to DTO with all validation info
     */
    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        // Check for stock issues and price changes
        boolean hasStockIssues = itemDTOs.stream()
                .anyMatch(item -> Boolean.FALSE.equals(item.getInStock()));
        boolean hasPriceChanges = itemDTOs.stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getPriceChanged()));
        boolean isValid = !hasStockIssues;

        // Collect warnings
        List<String> warnings = new ArrayList<>();
        for (CartItemDTO item : itemDTOs) {
            if (Boolean.FALSE.equals(item.getInStock())) {
                warnings.add(item.getItemName() + " is out of stock");
            } else if (item.getStockWarning() != null) {
                warnings.add(item.getStockWarning());
            }
            if (item.getPriceChangeMessage() != null) {
                warnings.add(item.getPriceChangeMessage());
            }
        }

        // Calculate expiration
        LocalDateTime expiresAt = cartExpirationService.getCartExpirationTime(cart);
        long minutesUntilExpiry = cartExpirationService.getMinutesUntilExpiry(cart);

        if (cartExpirationService.isAboutToExpire(cart)) {
            warnings.add("Your cart will expire in " + minutesUntilExpiry + " minutes. Complete your order soon!");
        }

        return CartDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .kitchenId(cart.getKitchenId())
                .kitchenName(cart.getKitchenName())
                .items(itemDTOs)
                .subtotal(cart.getSubtotal())
                .deliveryFee(cart.getDeliveryFee())
                .discount(cart.getDiscountAmount())
                .couponCode(cart.getCouponCode())
                .couponDescription(cart.getCouponDescription())
                .total(cart.getTotal())
                .itemCount(cart.getItemCount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .isValid(isValid)
                .hasStockIssues(hasStockIssues)
                .hasPriceChanges(hasPriceChanges)
                .warnings(warnings)
                .expiresAt(expiresAt)
                .minutesUntilExpiry(minutesUntilExpiry)
                .build();
    }

    /**
     * Map CartItem entity to DTO with stock and price info
     */
    private CartItemDTO mapItemToDTO(CartItem item) {
        // Check for price changes
        boolean priceChanged = item.hasPriceChanged();
        BigDecimal priceDifference = item.getPriceDifference();
        String priceChangeMessage = null;

        if (priceChanged && priceDifference != null) {
            if (priceDifference.compareTo(BigDecimal.ZERO) > 0) {
                priceChangeMessage = item.getItemName() + " price increased by RM" + priceDifference.abs();
            } else {
                priceChangeMessage = item.getItemName() + " price decreased by RM" + priceDifference.abs();
            }
        }

        // Stock warning
        String stockWarning = null;
        if (item.getAvailableStock() != null && item.getAvailableStock() <= 5 && item.getAvailableStock() > 0) {
            stockWarning = "Only " + item.getAvailableStock() + " left of " + item.getItemName();
        }

        return CartItemDTO.builder()
                .cartItemId(item.getId())
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getItemTotal())
                .specialRequests(item.getSpecialRequests())
                .imageUrl(item.getImageUrl())
                .inStock(item.getInStock())
                .availableStock(item.getAvailableStock())
                .stockWarning(stockWarning)
                .originalPrice(item.getOriginalPrice())
                .priceChanged(priceChanged)
                .priceDifference(priceDifference)
                .priceChangeMessage(priceChangeMessage)
                .addedAt(item.getAddedAt())
                .build();
    }

    /**
     * Refresh cart - updates stock availability and prices from Menu Service
     */
    public CartDTO refreshCart(Long userId) {
        log.info("Refreshing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException("CART_NOT_FOUND", "Cart not found"));

        if (cart.getItems().isEmpty()) {
            return mapToDTO(cart);
        }

        boolean hasChanges = false;

        for (CartItem item : cart.getItems()) {
            Optional<MenuItemDTO> menuItemOpt = menuServiceClient.getMenuItem(item.getItemId());

            if (menuItemOpt.isEmpty()) {
                // Item no longer exists
                item.setInStock(false);
                item.setAvailableStock(0);
                hasChanges = true;
                continue;
            }

            MenuItemDTO menuItem = menuItemOpt.get();

            // Update stock status
            boolean wasInStock = Boolean.TRUE.equals(item.getInStock());
            boolean isNowInStock = Boolean.TRUE.equals(menuItem.getIsAvailable()) &&
                                   menuItem.hasStock(item.getQuantity());

            if (wasInStock != isNowInStock) {
                hasChanges = true;
            }

            item.setInStock(isNowInStock);
            item.setAvailableStock(menuItem.getStockQuantity());

            // Check for price changes
            BigDecimal currentPrice = menuItem.getUnitPrice();
            if (currentPrice != null && !currentPrice.equals(item.getUnitPrice())) {
                log.info("Price changed for item {}: {} -> {}",
                        item.getItemId(), item.getUnitPrice(), currentPrice);
                item.setCurrentMenuPrice(currentPrice);
                item.setUnitPrice(currentPrice); // Update to new price
                item.setPriceChanged(item.getOriginalPrice() != null &&
                                     item.getOriginalPrice().compareTo(currentPrice) != 0);
                hasChanges = true;
            }

            cartItemRepository.save(item);
        }

        if (hasChanges) {
            cart = cartRepository.save(cart);
            log.info("Cart refreshed with changes");
        } else {
            log.info("Cart refreshed - no changes");
        }

        return mapToDTO(cart);
    }

    /**
     * Validate cart before checkout - ensures all items are in stock and available
     */
    public CartDTO validateCartForCheckout(Long userId) {
        log.info("Validating cart for checkout: user {}", userId);

        // First refresh the cart to get latest stock/prices
        CartDTO cart = refreshCart(userId);

        if (!cart.getIsValid()) {
            throw new ApplicationException("CART_INVALID",
                    "Cart contains items that are out of stock. Please review your cart.");
        }

        if (cart.getHasStockIssues()) {
            throw new ApplicationException("STOCK_ISSUES",
                    "Some items in your cart have stock issues: " + String.join(", ", cart.getWarnings()));
        }

        return cart;
    }
}
