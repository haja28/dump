package com.makanforyou.kitchen.repository;

import com.makanforyou.kitchen.entity.Kitchen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Kitchen entity
 */
@Repository
public interface KitchenRepository extends JpaRepository<Kitchen, Long> {
    Optional<Kitchen> findByOwnerUserId(Long ownerUserId);
    Page<Kitchen> findByApprovalStatusAndIsActiveTrue(Kitchen.ApprovalStatus status, Pageable pageable);
    Page<Kitchen> findByIsActiveTrue(Pageable pageable);
    Page<Kitchen> findByKitchenNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    Page<Kitchen> findByCityAndIsActiveTrue(String city, Pageable pageable);

    @Query("SELECT k FROM Kitchen k WHERE k.approvalStatus = 'APPROVED' AND k.isActive = true AND " +
           "(LOWER(k.kitchenName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Kitchen> searchApprovedKitchens(String query, Pageable pageable);
}
