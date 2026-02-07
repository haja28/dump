package com.makanforyou.menu.service;

import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.menu.dto.MenuItemDTO;
import com.makanforyou.menu.dto.MenuItemRequest;
import com.makanforyou.menu.dto.MenuSearchFilter;
import com.makanforyou.menu.dto.MenuLabelDTO;
import com.makanforyou.menu.entity.MenuItem;
import com.makanforyou.menu.entity.MenuLabel;
import com.makanforyou.menu.repository.MenuItemRepository;
import com.makanforyou.menu.repository.MenuLabelRepository;
import com.makanforyou.menu.specification.MenuItemSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for menu item operations with advanced search and filtering
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuLabelRepository labelRepository;

    /**
     * Create menu item
     */
    public MenuItemDTO createMenuItem(Long kitchenId, MenuItemRequest request) {
        log.info("Creating menu item for kitchen: {}", kitchenId);

        MenuItem item = MenuItem.builder()
                .kitchenId(kitchenId)
                .itemName(request.getItemName())
                .description(request.getDescription())
                .ingredients(request.getIngredients())
                .allergyIndication(request.getAllergyIndication())
                .cost(request.getCost())
                .imagePath(request.getImagePath())
                .availableTiming(request.getAvailableTiming())
                .preparationTimeMinutes(request.getPreparationTimeMinutes() != null ?
                        request.getPreparationTimeMinutes() : 30)
                .quantityAvailable(request.getQuantityAvailable())
                .isVeg(request.getIsVeg() != null ? request.getIsVeg() : true)
                .isHalal(request.getIsHalal() != null ? request.getIsHalal() : false)
                .spicyLevel(request.getSpicyLevel() != null ? request.getSpicyLevel() : 1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Add labels
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            Set<MenuLabel> labels = new HashSet<>();
            for (Long labelId : request.getLabelIds()) {
                MenuLabel label = labelRepository.findById(labelId)
                        .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                                "Label not found with ID: " + labelId));
                labels.add(label);
            }
            item.setLabels(labels);
        }

        item = menuItemRepository.save(item);
        log.info("Menu item created with ID: {}", item.getId());
        return mapToDTO(item);
    }

    /**
     * Get menu item by ID
     */
    public MenuItemDTO getMenuItemById(Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException("MENU_ITEM_NOT_FOUND",
                        "Menu item not found"));
        return mapToDTO(item);
    }

    /**
     * Get all menu items for a kitchen
     */
    public PagedResponse<MenuItemDTO> getKitchenMenuItems(Long kitchenId, Pageable pageable) {
        log.info("Fetching menu items for kitchen: {}", kitchenId);
        Page<MenuItem> page = menuItemRepository.findByKitchenIdAndIsActiveTrue(kitchenId, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Advanced search with filters
     */
    public PagedResponse<MenuItemDTO> searchMenuItems(MenuSearchFilter filter, Pageable pageable) {
        log.info("Searching menu items with filter: {}", filter);

        Specification<MenuItem> spec = MenuItemSpecification.withDynamicFilters(filter);
        Page<MenuItem> page = menuItemRepository.findAll(spec, pageable);

        return buildPagedResponse(page);
    }

    /**
     * Update menu item
     */
    public MenuItemDTO updateMenuItem(Long itemId, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException("MENU_ITEM_NOT_FOUND",
                        "Menu item not found"));

        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setIngredients(request.getIngredients());
        item.setAllergyIndication(request.getAllergyIndication());
        item.setCost(request.getCost());
        item.setImagePath(request.getImagePath());
        item.setAvailableTiming(request.getAvailableTiming());
        item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        item.setQuantityAvailable(request.getQuantityAvailable());
        item.setIsVeg(request.getIsVeg());
        item.setIsHalal(request.getIsHalal());
        item.setSpicyLevel(request.getSpicyLevel());
        item.setUpdatedAt(LocalDateTime.now());

        // Update labels
        if (request.getLabelIds() != null) {
            Set<MenuLabel> labels = new HashSet<>();
            for (Long labelId : request.getLabelIds()) {
                MenuLabel label = labelRepository.findById(labelId)
                        .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                                "Label not found with ID: " + labelId));
                labels.add(label);
            }
            item.setLabels(labels);
        }

        item = menuItemRepository.save(item);
        log.info("Menu item updated with ID: {}", itemId);
        return mapToDTO(item);
    }

    /**
     * Deactivate menu item
     */
    public MenuItemDTO deactivateMenuItem(Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException("MENU_ITEM_NOT_FOUND",
                        "Menu item not found"));

        item.setIsActive(false);
        item.setUpdatedAt(LocalDateTime.now());
        item = menuItemRepository.save(item);

        log.info("Menu item deactivated with ID: {}", itemId);
        return mapToDTO(item);
    }

    /**
     * Map MenuItem to DTO
     */
    private MenuItemDTO mapToDTO(MenuItem item) {
        return MenuItemDTO.builder()
                .id(item.getId())
                .kitchenId(item.getKitchenId())
                .itemName(item.getItemName())
                .description(item.getDescription())
                .ingredients(item.getIngredients())
                .allergyIndication(item.getAllergyIndication())
                .cost(item.getCost())
                .imagePath(item.getImagePath())
                .availableTiming(item.getAvailableTiming())
                .isActive(item.getIsActive())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .quantityAvailable(item.getQuantityAvailable())
                .isVeg(item.getIsVeg())
                .isHalal(item.getIsHalal())
                .spicyLevel(item.getSpicyLevel())
                .rating(item.getRating())
                .labels(item.getLabels().stream()
                        .map(l -> MenuLabelDTO.builder()
                                .id(l.getId())
                                .name(l.getName())
                                .description(l.getDescription())
                                .isActive(l.getIsActive())
                                .build())
                        .collect(Collectors.toSet()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * Build paged response
     */
    private PagedResponse<MenuItemDTO> buildPagedResponse(Page<MenuItem> page) {
        return PagedResponse.<MenuItemDTO>builder()
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
