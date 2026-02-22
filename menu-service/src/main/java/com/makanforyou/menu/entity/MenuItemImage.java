package com.makanforyou.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing menu item images with thumbnail support
 * Multiple images can be associated with a single menu item
 */
@Entity
@Table(name = "menu_item_images", indexes = {
        @Index(name = "idx_menu_item_id", columnList = "menu_item_id"),
        @Index(name = "idx_uuid", columnList = "uuid"),
        @Index(name = "idx_is_primary", columnList = "is_primary")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    /**
     * UUID for unique identification across all kitchens
     */
    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    /**
     * Original filename uploaded by user
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Path to the original full-size image
     */
    @Column(name = "original_image_path", nullable = false, length = 500)
    private String originalImagePath;

    /**
     * Path to the thumbnail image (optional - generated on demand)
     */
    @Column(name = "thumbnail_image_path", length = 500)
    private String thumbnailImagePath;

    /**
     * MIME type of the image (e.g., image/jpeg, image/png)
     */
    @Column(name = "content_type", length = 50)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Original image width in pixels
     */
    @Column(name = "image_width")
    private Integer imageWidth;

    /**
     * Original image height in pixels
     */
    @Column(name = "image_height")
    private Integer imageHeight;

    /**
     * Thumbnail image width in pixels
     */
    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    /**
     * Thumbnail image height in pixels
     */
    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    /**
     * Display order of images (lower number = higher priority)
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this is the primary/featured image for the menu item
     */
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * Whether the image is active
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Alt text for accessibility
     */
    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

