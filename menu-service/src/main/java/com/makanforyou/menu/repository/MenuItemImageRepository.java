package com.makanforyou.menu.repository;

import com.makanforyou.menu.entity.MenuItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MenuItemImage entity
 */
@Repository
public interface MenuItemImageRepository extends JpaRepository<MenuItemImage, Long> {

    /**
     * Find all active images for a menu item ordered by display order
     */
    List<MenuItemImage> findByMenuItemIdAndIsActiveTrueOrderByDisplayOrderAsc(Long menuItemId);

    /**
     * Find all images for a menu item (including inactive)
     */
    List<MenuItemImage> findByMenuItemIdOrderByDisplayOrderAsc(Long menuItemId);

    /**
     * Find an image by UUID
     */
    Optional<MenuItemImage> findByUuid(String uuid);

    /**
     * Find an image by ID and menu item ID
     */
    Optional<MenuItemImage> findByIdAndMenuItemId(Long imageId, Long menuItemId);

    /**
     * Find the primary image for a menu item
     */
    Optional<MenuItemImage> findByMenuItemIdAndIsPrimaryTrue(Long menuItemId);

    /**
     * Count active images for a menu item
     */
    long countByMenuItemIdAndIsActiveTrue(Long menuItemId);

    /**
     * Get max display order for a menu item
     */
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MenuItemImage m WHERE m.menuItem.id = :menuItemId")
    Integer findMaxDisplayOrderByMenuItemId(@Param("menuItemId") Long menuItemId);

    /**
     * Clear primary flag for all images of a menu item
     */
    @Modifying
    @Query("UPDATE MenuItemImage m SET m.isPrimary = false WHERE m.menuItem.id = :menuItemId")
    void clearPrimaryFlagForMenuItem(@Param("menuItemId") Long menuItemId);

    /**
     * Delete all images for a menu item
     */
    void deleteByMenuItemId(Long menuItemId);

    /**
     * Find all images for multiple menu items
     */
    List<MenuItemImage> findByMenuItemIdInAndIsActiveTrueOrderByMenuItemIdAscDisplayOrderAsc(List<Long> menuItemIds);
}

