package com.makanforyou.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MenuLabel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuLabelDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}
