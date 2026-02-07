package com.makanforyou.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for MenuItem response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    private Long id;
    private Long kitchenId;
    private String itemName;
    private String description;
    private String ingredients;
    private String allergyIndication;
    private BigDecimal cost;
    private String imagePath;
    private String availableTiming;
    private Boolean isActive;
    private Integer preparationTimeMinutes;
    private Integer quantityAvailable;
    private Boolean isVeg;
    private Boolean isHalal;
    private Integer spicyLevel;
    private Double rating;
    private Set<MenuLabelDTO> labels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
