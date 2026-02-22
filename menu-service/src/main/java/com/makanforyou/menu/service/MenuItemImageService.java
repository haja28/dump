package com.makanforyou.menu.service;

import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.menu.config.StorageProperties;
import com.makanforyou.menu.dto.MenuItemImageDTO;
import com.makanforyou.menu.entity.MenuItem;
import com.makanforyou.menu.entity.MenuItemImage;
import com.makanforyou.menu.repository.MenuItemImageRepository;
import com.makanforyou.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling menu item image uploads with thumbnail generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemImageService {

    private final MenuItemImageRepository imageRepository;
    private final MenuItemRepository menuItemRepository;
    private final StorageProperties storageProperties;

    /**
     * Upload a single image for a menu item
     */
    public MenuItemImageDTO uploadImage(Long menuItemId, MultipartFile file, String altText, Boolean isPrimary) {
        log.info("Uploading image for menu item: {}", menuItemId);

        // Validate menu item exists
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ApplicationException("MENU_ITEM_NOT_FOUND",
                        "Menu item not found with ID: " + menuItemId));

        // Validate file
        validateFile(file);

        // Generate UUID for uniqueness
        String uuid = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String storedFileName = uuid + fileExtension;

        try {
            // Create directories if not exist
            createDirectories();

            // Save original image
            Path originalPath = Paths.get(storageProperties.getOriginalsPath(), storedFileName);
            Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

            // Read image dimensions
            BufferedImage originalImage = ImageIO.read(originalPath.toFile());
            if (originalImage == null) {
                throw new ApplicationException("INVALID_IMAGE", "Unable to read image file");
            }
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // Create and save thumbnail
            Path thumbnailPath = Paths.get(storageProperties.getThumbnailsPath(), storedFileName);
            int[] thumbnailDimensions = createThumbnail(originalImage, thumbnailPath, fileExtension);

            // Get next display order
            Integer maxOrder = imageRepository.findMaxDisplayOrderByMenuItemId(menuItemId);
            int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

            // Handle primary flag
            if (Boolean.TRUE.equals(isPrimary)) {
                imageRepository.clearPrimaryFlagForMenuItem(menuItemId);
            }

            // If this is the first image, make it primary
            long imageCount = imageRepository.countByMenuItemIdAndIsActiveTrue(menuItemId);
            if (imageCount == 0) {
                isPrimary = true;
            }

            // Create entity
            MenuItemImage image = MenuItemImage.builder()
                    .menuItem(menuItem)
                    .uuid(uuid)
                    .originalFilename(file.getOriginalFilename())
                    .originalImagePath(storageProperties.getOriginalsPath() + "/" + storedFileName)
                    .thumbnailImagePath(storageProperties.getThumbnailsPath() + "/" + storedFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .imageWidth(originalWidth)
                    .imageHeight(originalHeight)
                    .thumbnailWidth(thumbnailDimensions[0])
                    .thumbnailHeight(thumbnailDimensions[1])
                    .displayOrder(nextOrder)
                    .isPrimary(Boolean.TRUE.equals(isPrimary))
                    .isActive(true)
                    .altText(altText)
                    .build();

            image = imageRepository.save(image);
            log.info("Image uploaded successfully with UUID: {} for menu item: {}", uuid, menuItemId);

            return mapToDTO(image);

        } catch (IOException e) {
            log.error("Failed to upload image for menu item: {}", menuItemId, e);
            throw new ApplicationException("FILE_UPLOAD_FAILED",
                    "Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Upload multiple images for a menu item
     */
    public List<MenuItemImageDTO> uploadImages(Long menuItemId, List<MultipartFile> files, String altText) {
        log.info("Uploading {} images for menu item: {}", files.size(), menuItemId);

        return files.stream()
                .map(file -> uploadImage(menuItemId, file, altText, false))
                .collect(Collectors.toList());
    }

    /**
     * Get all active images for a menu item
     */
    @Transactional(readOnly = true)
    public List<MenuItemImageDTO> getMenuItemImages(Long menuItemId) {
        List<MenuItemImage> images = imageRepository.findByMenuItemIdAndIsActiveTrueOrderByDisplayOrderAsc(menuItemId);
        return images.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get image by UUID
     */
    @Transactional(readOnly = true)
    public MenuItemImageDTO getImageByUuid(String uuid) {
        MenuItemImage image = imageRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with UUID: " + uuid));
        return mapToDTO(image);
    }

    /**
     * Get image by ID
     */
    @Transactional(readOnly = true)
    public MenuItemImageDTO getImageById(Long imageId) {
        MenuItemImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId));
        return mapToDTO(image);
    }

    /**
     * Set an image as primary
     */
    public MenuItemImageDTO setPrimaryImage(Long menuItemId, Long imageId) {
        MenuItemImage image = imageRepository.findByIdAndMenuItemId(imageId, menuItemId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId + " for menu item: " + menuItemId));

        // Clear primary flag for all images of this menu item
        imageRepository.clearPrimaryFlagForMenuItem(menuItemId);

        // Set this image as primary
        image.setIsPrimary(true);
        image = imageRepository.save(image);

        log.info("Image {} set as primary for menu item: {}", imageId, menuItemId);
        return mapToDTO(image);
    }

    /**
     * Update image display order
     */
    public MenuItemImageDTO updateDisplayOrder(Long menuItemId, Long imageId, Integer displayOrder) {
        MenuItemImage image = imageRepository.findByIdAndMenuItemId(imageId, menuItemId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId + " for menu item: " + menuItemId));

        image.setDisplayOrder(displayOrder);
        image = imageRepository.save(image);

        log.info("Image {} display order updated to {} for menu item: {}", imageId, displayOrder, menuItemId);
        return mapToDTO(image);
    }

    /**
     * Update image alt text
     */
    public MenuItemImageDTO updateAltText(Long menuItemId, Long imageId, String altText) {
        MenuItemImage image = imageRepository.findByIdAndMenuItemId(imageId, menuItemId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId + " for menu item: " + menuItemId));

        image.setAltText(altText);
        image = imageRepository.save(image);

        log.info("Image {} alt text updated for menu item: {}", imageId, menuItemId);
        return mapToDTO(image);
    }

    /**
     * Deactivate (soft delete) an image
     */
    public void deactivateImage(Long menuItemId, Long imageId) {
        MenuItemImage image = imageRepository.findByIdAndMenuItemId(imageId, menuItemId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId + " for menu item: " + menuItemId));

        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
        image.setIsActive(false);
        image.setIsPrimary(false);
        imageRepository.save(image);

        // If this was the primary image, set the first active image as primary
        if (wasPrimary) {
            List<MenuItemImage> activeImages = imageRepository
                    .findByMenuItemIdAndIsActiveTrueOrderByDisplayOrderAsc(menuItemId);
            if (!activeImages.isEmpty()) {
                MenuItemImage newPrimary = activeImages.get(0);
                newPrimary.setIsPrimary(true);
                imageRepository.save(newPrimary);
            }
        }

        log.info("Image {} deactivated for menu item: {}", imageId, menuItemId);
    }

    /**
     * Delete an image permanently (including files)
     */
    public void deleteImage(Long menuItemId, Long imageId) {
        MenuItemImage image = imageRepository.findByIdAndMenuItemId(imageId, menuItemId)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId + " for menu item: " + menuItemId));

        // Delete physical files
        try {
            Files.deleteIfExists(Paths.get(image.getOriginalImagePath()));
            Files.deleteIfExists(Paths.get(image.getThumbnailImagePath()));
        } catch (IOException e) {
            log.warn("Failed to delete image files for image ID: {}", imageId, e);
        }

        // Check if was primary before deleting
        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
        imageRepository.delete(image);

        // If this was the primary image, set the first remaining image as primary
        if (wasPrimary) {
            List<MenuItemImage> remainingImages = imageRepository
                    .findByMenuItemIdAndIsActiveTrueOrderByDisplayOrderAsc(menuItemId);
            if (!remainingImages.isEmpty()) {
                MenuItemImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                imageRepository.save(newPrimary);
            }
        }

        log.info("Image {} permanently deleted for menu item: {}", imageId, menuItemId);
    }

    /**
     * Get the physical file path for an image (original or thumbnail)
     */
    @Transactional(readOnly = true)
    public Path getImageFilePath(String uuid, boolean thumbnail) {
        MenuItemImage image = imageRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApplicationException("IMAGE_NOT_FOUND",
                        "Image not found with UUID: " + uuid));

        String path = thumbnail ? image.getThumbnailImagePath() : image.getOriginalImagePath();
        return Paths.get(path);
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException("EMPTY_FILE", "File is empty or not provided");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new ApplicationException("FILE_TOO_LARGE",
                    "File size exceeds maximum allowed size of " +
                            (storageProperties.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        String contentType = file.getContentType();
        boolean validContentType = Arrays.stream(storageProperties.getAllowedContentTypes())
                .anyMatch(type -> type.equals(contentType));

        if (!validContentType) {
            throw new ApplicationException("INVALID_FILE_TYPE",
                    "File type not allowed. Allowed types: " +
                            String.join(", ", storageProperties.getAllowedContentTypes()));
        }
    }

    /**
     * Create storage directories if they don't exist
     */
    private void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(storageProperties.getOriginalsPath()));
        Files.createDirectories(Paths.get(storageProperties.getThumbnailsPath()));
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Create thumbnail from original image
     * Returns array with [width, height] of created thumbnail
     */
    private int[] createThumbnail(BufferedImage original, Path thumbnailPath, String extension) throws IOException {
        int targetWidth = storageProperties.getThumbnailWidth();
        int targetHeight = storageProperties.getThumbnailHeight();

        // Calculate dimensions maintaining aspect ratio
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        double ratio = Math.min(
                (double) targetWidth / originalWidth,
                (double) targetHeight / originalHeight
        );

        int scaledWidth = (int) (originalWidth * ratio);
        int scaledHeight = (int) (originalHeight * ratio);

        // Create scaled image
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // Use high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // Write thumbnail with compression
        String formatName = getFormatName(extension);
        if ("jpg".equals(formatName) || "jpeg".equals(formatName)) {
            writeJpegWithCompression(scaledImage, thumbnailPath.toFile());
        } else {
            ImageIO.write(scaledImage, formatName, thumbnailPath.toFile());
        }

        return new int[]{scaledWidth, scaledHeight};
    }

    /**
     * Write JPEG with compression
     */
    private void writeJpegWithCompression(BufferedImage image, File outputFile) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer found");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(storageProperties.getImageQuality());
        }

        try (OutputStream os = Files.newOutputStream(outputFile.toPath());
             ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Get image format name from extension
     */
    private String getFormatName(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "jpg";
            case ".png" -> "png";
            case ".gif" -> "gif";
            case ".webp" -> "webp";
            default -> "jpg";
        };
    }

    /**
     * Map entity to DTO
     */
    private MenuItemImageDTO mapToDTO(MenuItemImage image) {
        String baseUrl = storageProperties.getBaseUrl();
        return MenuItemImageDTO.builder()
                .id(image.getId())
                .menuItemId(image.getMenuItem().getId())
                .uuid(image.getUuid())
                .originalFilename(image.getOriginalFilename())
                .originalImageUrl(baseUrl + "/" + image.getUuid() + "/original")
                .thumbnailImageUrl(baseUrl + "/" + image.getUuid() + "/thumbnail")
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .imageWidth(image.getImageWidth())
                .imageHeight(image.getImageHeight())
                .thumbnailWidth(image.getThumbnailWidth())
                .thumbnailHeight(image.getThumbnailHeight())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .isActive(image.getIsActive())
                .altText(image.getAltText())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}

