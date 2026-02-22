package com.makanforyou.menu.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Request DTO for creating/updating MenuItem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 100, message = "Item name must be between 2 and 100 characters")
    private String itemName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 500, message = "Ingredients must not exceed 500 characters")
    private String ingredients;

    @Size(max = 200, message = "Allergy indication must not exceed 200 characters")
    private String allergyIndication;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.01", message = "Cost must be greater than 0")
    @DecimalMax(value = "9999.99", message = "Cost must be less than 10000")
    private BigDecimal cost;

    @Size(max = 255)
    private String imagePath;

    @Size(max = 100)
    private String availableTiming;

    @Min(1)
    @Max(180)
    private Integer preparationTimeMinutes;

    @Min(0)
    private Integer quantityAvailable;

    @Builder.Default
    private Boolean isVeg = true;

    @Builder.Default
    private Boolean isHalal = false;

    @Min(1)
    @Max(5)
    private Integer spicyLevel;

    private Set<Long> labelIds;
}
