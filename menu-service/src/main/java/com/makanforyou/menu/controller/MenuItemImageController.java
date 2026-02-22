package com.makanforyou.menu.controller;

import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.menu.dto.MenuItemImageDTO;
import com.makanforyou.menu.service.MenuItemImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Controller for menu item image upload and management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Menu Item Images", description = "Menu item image upload and management endpoints")
public class MenuItemImageController {

    private final MenuItemImageService imageService;

    /**
     * Upload a single image for a menu item
     * POST /api/v1/menu-items/{menuItemId}/images
     */
    @PostMapping(value = "/menu-items/{menuItemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Upload a single image for a menu item")
    public ResponseEntity<ApiResponse<MenuItemImageDTO>> uploadImage(
            @PathVariable Long menuItemId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {

        log.info("Uploading image for menu item: {}", menuItemId);
        MenuItemImageDTO image = imageService.uploadImage(menuItemId, file, altText, isPrimary);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(image, "Image uploaded successfully"));
    }

    /**
     * Upload multiple images for a menu item
     * POST /api/v1/menu-items/{menuItemId}/images/batch
     */
    @PostMapping(value = "/menu-items/{menuItemId}/images/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple images", description = "Upload multiple images for a menu item")
    public ResponseEntity<ApiResponse<List<MenuItemImageDTO>>> uploadImages(
            @PathVariable Long menuItemId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "altText", required = false) String altText) {

        log.info("Uploading {} images for menu item: {}", files.size(), menuItemId);
        List<MenuItemImageDTO> images = imageService.uploadImages(menuItemId, files, altText);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(images, "Images uploaded successfully"));
    }

    /**
     * Get all images for a menu item
     * GET /api/v1/menu-items/{menuItemId}/images
     */
    @GetMapping("/menu-items/{menuItemId}/images")
    @Operation(summary = "Get menu item images", description = "Get all active images for a menu item")
    public ResponseEntity<ApiResponse<List<MenuItemImageDTO>>> getMenuItemImages(
            @PathVariable Long menuItemId) {

        log.info("Getting images for menu item: {}", menuItemId);
        List<MenuItemImageDTO> images = imageService.getMenuItemImages(menuItemId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }

    /**
     * Get image details by ID
     * GET /api/v1/menu-items/{menuItemId}/images/{imageId}
     */
    @GetMapping("/menu-items/{menuItemId}/images/{imageId}")
    @Operation(summary = "Get image details", description = "Get image details by ID")
    public ResponseEntity<ApiResponse<MenuItemImageDTO>> getImage(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId) {

        log.info("Getting image {} for menu item: {}", imageId, menuItemId);
        MenuItemImageDTO image = imageService.getImageById(imageId);
        return ResponseEntity.ok(ApiResponse.success(image));
    }

    /**
     * Set an image as primary
     * PATCH /api/v1/menu-items/{menuItemId}/images/{imageId}/primary
     */
    @PatchMapping("/menu-items/{menuItemId}/images/{imageId}/primary")
    @Operation(summary = "Set primary image", description = "Set an image as the primary/featured image")
    public ResponseEntity<ApiResponse<MenuItemImageDTO>> setPrimaryImage(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId) {

        log.info("Setting image {} as primary for menu item: {}", imageId, menuItemId);
        MenuItemImageDTO image = imageService.setPrimaryImage(menuItemId, imageId);
        return ResponseEntity.ok(ApiResponse.success(image, "Image set as primary"));
    }

    /**
     * Update image display order
     * PATCH /api/v1/menu-items/{menuItemId}/images/{imageId}/order
     */
    @PatchMapping("/menu-items/{menuItemId}/images/{imageId}/order")
    @Operation(summary = "Update display order", description = "Update the display order of an image")
    public ResponseEntity<ApiResponse<MenuItemImageDTO>> updateDisplayOrder(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId,
            @RequestParam("displayOrder") Integer displayOrder) {

        log.info("Updating display order for image {} to {} for menu item: {}", imageId, displayOrder, menuItemId);
        MenuItemImageDTO image = imageService.updateDisplayOrder(menuItemId, imageId, displayOrder);
        return ResponseEntity.ok(ApiResponse.success(image, "Display order updated"));
    }

    /**
     * Update image alt text
     * PATCH /api/v1/menu-items/{menuItemId}/images/{imageId}/alt-text
     */
    @PatchMapping("/menu-items/{menuItemId}/images/{imageId}/alt-text")
    @Operation(summary = "Update alt text", description = "Update the alt text of an image")
    public ResponseEntity<ApiResponse<MenuItemImageDTO>> updateAltText(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId,
            @RequestParam("altText") String altText) {

        log.info("Updating alt text for image {} for menu item: {}", imageId, menuItemId);
        MenuItemImageDTO image = imageService.updateAltText(menuItemId, imageId, altText);
        return ResponseEntity.ok(ApiResponse.success(image, "Alt text updated"));
    }

    /**
     * Deactivate an image (soft delete)
     * PATCH /api/v1/menu-items/{menuItemId}/images/{imageId}/deactivate
     */
    @PatchMapping("/menu-items/{menuItemId}/images/{imageId}/deactivate")
    @Operation(summary = "Deactivate image", description = "Soft delete an image")
    public ResponseEntity<ApiResponse<Void>> deactivateImage(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId) {

        log.info("Deactivating image {} for menu item: {}", imageId, menuItemId);
        imageService.deactivateImage(menuItemId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deactivated successfully"));
    }

    /**
     * Delete an image permanently
     * DELETE /api/v1/menu-items/{menuItemId}/images/{imageId}
     */
    @DeleteMapping("/menu-items/{menuItemId}/images/{imageId}")
    @Operation(summary = "Delete image", description = "Permanently delete an image and its files")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long menuItemId,
            @PathVariable Long imageId) {

        log.info("Deleting image {} for menu item: {}", imageId, menuItemId);
        imageService.deleteImage(menuItemId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }

    /**
     * Serve original image by UUID
     * GET /api/v1/images/{uuid}/original
     */
    @GetMapping("/images/{uuid}/original")
    @Operation(summary = "Get original image", description = "Serve the original full-size image")
    public ResponseEntity<Resource> getOriginalImage(@PathVariable String uuid) {
        return serveImage(uuid, false);
    }

    /**
     * Serve thumbnail image by UUID
     * GET /api/v1/images/{uuid}/thumbnail
     */
    @GetMapping("/images/{uuid}/thumbnail")
    @Operation(summary = "Get thumbnail image", description = "Serve the thumbnail image")
    public ResponseEntity<Resource> getThumbnailImage(@PathVariable String uuid) {
        return serveImage(uuid, true);
    }

    /**
     * Serve image file
     */
    private ResponseEntity<Resource> serveImage(String uuid, boolean thumbnail) {
        try {
            Path filePath = imageService.getImageFilePath(uuid, thumbnail);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // Cache for 1 year
                        .body(resource);
            } else {
                log.warn("Image file not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for image: {}", uuid, e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error reading image file: {}", uuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error serving image: {} - {}", uuid, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

