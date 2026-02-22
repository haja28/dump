package com.makanforyou.order.service;

import com.makanforyou.order.dto.MenuItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Mock Menu Service Client
 *
 * In production, this would be a Feign client or WebClient that calls the Menu Service.
 * For now, it provides mock data for testing stock validation and price updates.
 */
@Slf4j
@Service
public class MenuServiceClient {

    // Mock database of menu items with stock
    private static final Map<Long, MenuItemDTO> MOCK_MENU_ITEMS = new HashMap<>();
    private static final Random random = new Random();

    static {
        // Initialize mock menu items
        MOCK_MENU_ITEMS.put(1L, MenuItemDTO.builder()
                .itemId(1L)
                .kitchenId(1L)
                .kitchenName("Priya's Kitchen")
                .itemName("Chicken Biryani")
                .itemDescription("Authentic Hyderabadi biryani with tender chicken")
                .unitPrice(new BigDecimal("12.99"))
                .imageUrl("https://example.com/biryani.jpg")
                .isAvailable(true)
                .stockQuantity(50)
                .maxOrderQuantity(10)
                .build());

        MOCK_MENU_ITEMS.put(2L, MenuItemDTO.builder()
                .itemId(2L)
                .kitchenId(1L)
                .kitchenName("Priya's Kitchen")
                .itemName("Paneer Tikka Masala")
                .itemDescription("Creamy tomato-based curry with paneer")
                .unitPrice(new BigDecimal("10.50"))
                .imageUrl("https://example.com/paneer.jpg")
                .isAvailable(true)
                .stockQuantity(30)
                .maxOrderQuantity(5)
                .build());

        MOCK_MENU_ITEMS.put(3L, MenuItemDTO.builder()
                .itemId(3L)
                .kitchenId(1L)
                .kitchenName("Priya's Kitchen")
                .itemName("Butter Naan")
                .itemDescription("Soft buttery flatbread")
                .unitPrice(new BigDecimal("2.50"))
                .imageUrl("https://example.com/naan.jpg")
                .isAvailable(true)
                .stockQuantity(100)
                .maxOrderQuantity(20)
                .build());

        MOCK_MENU_ITEMS.put(4L, MenuItemDTO.builder()
                .itemId(4L)
                .kitchenId(2L)
                .kitchenName("Ahmad's Kitchen")
                .itemName("Nasi Lemak Special")
                .itemDescription("Coconut rice with sambal, fried chicken, and egg")
                .unitPrice(new BigDecimal("8.99"))
                .imageUrl("https://example.com/nasilemak.jpg")
                .isAvailable(true)
                .stockQuantity(25)
                .maxOrderQuantity(5)
                .build());

        // Item with limited stock
        MOCK_MENU_ITEMS.put(5L, MenuItemDTO.builder()
                .itemId(5L)
                .kitchenId(1L)
                .kitchenName("Priya's Kitchen")
                .itemName("Special Chef's Thali")
                .itemDescription("Limited daily special")
                .unitPrice(new BigDecimal("18.99"))
                .imageUrl("https://example.com/thali.jpg")
                .isAvailable(true)
                .stockQuantity(3) // Very limited!
                .maxOrderQuantity(2)
                .build());

        // Out of stock item
        MOCK_MENU_ITEMS.put(6L, MenuItemDTO.builder()
                .itemId(6L)
                .kitchenId(1L)
                .kitchenName("Priya's Kitchen")
                .itemName("Mango Lassi")
                .itemDescription("Fresh mango yogurt drink")
                .unitPrice(new BigDecimal("4.50"))
                .imageUrl("https://example.com/lassi.jpg")
                .isAvailable(false) // Out of stock!
                .stockQuantity(0)
                .maxOrderQuantity(10)
                .build());
    }

    /**
     * Get menu item by ID
     * In production, this would call the Menu Service API
     */
    public Optional<MenuItemDTO> getMenuItem(Long itemId) {
        log.debug("Fetching menu item: {}", itemId);

        // Simulate API call delay
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        MenuItemDTO item = MOCK_MENU_ITEMS.get(itemId);

        if (item == null) {
            // Generate a mock item for unknown IDs
            item = generateMockItem(itemId);
        }

        return Optional.ofNullable(item);
    }

    /**
     * Check stock availability for an item
     */
    public boolean checkStock(Long itemId, int requestedQuantity) {
        log.debug("Checking stock for item {} with quantity {}", itemId, requestedQuantity);

        Optional<MenuItemDTO> itemOpt = getMenuItem(itemId);
        if (itemOpt.isEmpty()) {
            return false;
        }

        MenuItemDTO item = itemOpt.get();
        return item.hasStock(requestedQuantity);
    }

    /**
     * Get current price for an item (may differ from cart price if prices changed)
     */
    public BigDecimal getCurrentPrice(Long itemId) {
        log.debug("Getting current price for item: {}", itemId);

        Optional<MenuItemDTO> itemOpt = getMenuItem(itemId);
        return itemOpt.map(MenuItemDTO::getUnitPrice).orElse(null);
    }

    /**
     * Validate item and return detailed information
     */
    public MenuItemDTO validateItem(Long itemId, int requestedQuantity) {
        log.debug("Validating item {} with quantity {}", itemId, requestedQuantity);

        Optional<MenuItemDTO> itemOpt = getMenuItem(itemId);
        if (itemOpt.isEmpty()) {
            return null;
        }

        MenuItemDTO item = itemOpt.get();

        // For demonstration, occasionally simulate price changes
        if (random.nextInt(100) < 10) { // 10% chance of price change
            BigDecimal priceChange = new BigDecimal(random.nextDouble() * 2 - 1).setScale(2, BigDecimal.ROUND_HALF_UP);
            item.setUnitPrice(item.getUnitPrice().add(priceChange));
            log.info("Simulated price change for item {}: {}", itemId, priceChange);
        }

        return item;
    }

    /**
     * Reserve stock for checkout (in production, this would be a distributed transaction)
     */
    public boolean reserveStock(Long itemId, int quantity) {
        log.info("Reserving {} units of item {}", quantity, itemId);

        MenuItemDTO item = MOCK_MENU_ITEMS.get(itemId);
        if (item == null || !item.hasStock(quantity)) {
            return false;
        }

        // Reduce stock (in production, this would be done via Menu Service)
        item.setStockQuantity(item.getStockQuantity() - quantity);
        return true;
    }

    /**
     * Release reserved stock (if order cancelled)
     */
    public void releaseStock(Long itemId, int quantity) {
        log.info("Releasing {} units of item {}", quantity, itemId);

        MenuItemDTO item = MOCK_MENU_ITEMS.get(itemId);
        if (item != null) {
            item.setStockQuantity(item.getStockQuantity() + quantity);
        }
    }

    /**
     * Generate mock item for testing
     */
    private MenuItemDTO generateMockItem(Long itemId) {
        return MenuItemDTO.builder()
                .itemId(itemId)
                .kitchenId(1L)
                .kitchenName("Sample Kitchen")
                .itemName("Menu Item " + itemId)
                .itemDescription("Delicious food item")
                .unitPrice(new BigDecimal("12.99"))
                .imageUrl("https://example.com/images/item.jpg")
                .isAvailable(true)
                .stockQuantity(100)
                .maxOrderQuantity(10)
                .build();
    }
}
