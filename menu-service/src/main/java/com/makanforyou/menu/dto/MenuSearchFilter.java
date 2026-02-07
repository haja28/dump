package com.makanforyou.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Filters for searching menu items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSearchFilter {
    private String query;
    private Long kitchenId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isVeg;
    private Boolean isHalal;
    private Integer minSpicyLevel;
    private Integer maxSpicyLevel;
    private Boolean isActive;
    private String sortBy; // rating_desc, price_asc, price_desc, newest
    private Set<String> labels;

    public MenuSearchFilter(String query) {
        this.query = query;
        this.isActive = true;
        this.sortBy = "rating_desc";
    }
}
