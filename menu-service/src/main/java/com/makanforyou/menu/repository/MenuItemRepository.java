package com.makanforyou.menu.repository;

import com.makanforyou.menu.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for MenuItem entity with advanced search capabilities
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {
    Optional<MenuItem> findByIdAndKitchenId(Long id, Long kitchenId);
    Page<MenuItem> findByKitchenIdAndIsActiveTrue(Long kitchenId, Pageable pageable);
    List<MenuItem> findByKitchenIdAndIsActiveTrue(Long kitchenId);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND " +
           "(LOWER(m.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.ingredients) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MenuItem> searchByQuery(String query, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.cost BETWEEN :minPrice AND :maxPrice")
    Page<MenuItem> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.isVeg = :isVeg")
    Page<MenuItem> filterByVegetarian(Boolean isVeg, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.isHalal = :isHalal")
    Page<MenuItem> filterByHalal(Boolean isHalal, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND m.spicyLevel BETWEEN :minLevel AND :maxLevel")
    Page<MenuItem> filterBySpicyLevel(Integer minLevel, Integer maxLevel, Pageable pageable);
}
