package com.makanforyou.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for MenuItemImage response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemImageDTO {

    private Long id;
    private Long menuItemId;
    private String uuid;
    private String originalFilename;
    private String originalImageUrl;
    private String thumbnailImageUrl;
    private String contentType;
    private Long fileSize;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer thumbnailWidth;
    private Integer thumbnailHeight;
    private Integer displayOrder;
    private Boolean isPrimary;
    private Boolean isActive;
    private String altText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

