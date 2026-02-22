package com.makanforyou.menu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for file storage
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
@Validated
public class StorageProperties {

    /**
     * Base directory for storing uploaded files
     */
    @NotBlank
    private String uploadDir = "./uploads";

    /**
     * Subdirectory for menu item images
     */
    private String menuImagesDir = "menu-images";

    /**
     * Subdirectory for original images
     */
    private String originalsDir = "originals";

    /**
     * Subdirectory for thumbnail images
     */
    private String thumbnailsDir = "thumbnails";

    /**
     * Thumbnail width in pixels
     */
    @Min(50)
    private int thumbnailWidth = 300;

    /**
     * Thumbnail height in pixels
     */
    @Min(50)
    private int thumbnailHeight = 300;

    /**
     * Maximum file size in bytes (default 10MB)
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * Allowed content types
     */
    private String[] allowedContentTypes = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    /**
     * Base URL for serving images
     */
    private String baseUrl = "/api/v1/images";

    /**
     * Image quality for compression (0.0 - 1.0)
     */
    private float imageQuality = 0.85f;

    /**
     * Get full path for original images
     */
    public String getOriginalsPath() {
        return uploadDir + "/" + menuImagesDir + "/" + originalsDir;
    }

    /**
     * Get full path for thumbnail images
     */
    public String getThumbnailsPath() {
        return uploadDir + "/" + menuImagesDir + "/" + thumbnailsDir;
    }
}

