package com.makanforyou.kitchen.service;

import com.makanforyou.common.dto.PagedResponse;
import com.makanforyou.common.dto.PaginationMetadata;
import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.kitchen.dto.KitchenDTO;
import com.makanforyou.kitchen.dto.KitchenRegistrationRequest;
import com.makanforyou.kitchen.entity.Kitchen;
import com.makanforyou.kitchen.repository.KitchenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Service for kitchen operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KitchenService {

    private final KitchenRepository kitchenRepository;

    /**
     * Register new kitchen
     */
    public KitchenDTO registerKitchen(Long userId, KitchenRegistrationRequest request) {
        log.info("Registering new kitchen for user: {}", userId);

        // Validate required fields
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new ApplicationException("INVALID_REQUEST",
                    "Address is required and cannot be empty");
        }

        // Check if user already has a kitchen
        if (kitchenRepository.findByOwnerUserId(userId).isPresent()) {
            throw new ApplicationException("KITCHEN_ALREADY_EXISTS",
                    "User already has a registered kitchen");
        }

        Kitchen kitchen = Kitchen.builder()
                .ownerUserId(userId)
                .kitchenName(request.getKitchenName())
                .ownerName(request.getOwnerName())
                .description(request.getDescription())
                .address(request.getAddress().trim())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .deliveryArea(request.getDeliveryArea())
                .cuisineTypes(request.getCuisineTypes())
                .ownerContact(request.getOwnerContact())
                .ownerEmail(request.getOwnerEmail())
                .alternateContact(request.getAlternateContact())
                .alternateEmail(request.getAlternateEmail())
                .approvalStatus(Kitchen.ApprovalStatus.PENDING)
                .isActive(true)
                .verified(false)
                .build();

        kitchen = kitchenRepository.save(kitchen);
        log.info("Kitchen registered successfully with ID: {}", kitchen.getId());
        return mapToDTO(kitchen);
    }

    /**
     * Get kitchen by ID
     */
    public KitchenDTO getKitchenById(Long kitchenId) {
        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));
        return mapToDTO(kitchen);
    }

    /**
     * Get kitchen by owner user ID
     */
    public KitchenDTO getKitchenByOwnerUserId(Long userId) {
        Kitchen kitchen = kitchenRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found for user"));
        return mapToDTO(kitchen);
    }

    /**
     * Get all approved and active kitchens
     */
    public PagedResponse<KitchenDTO> getApprovedKitchens(Pageable pageable) {
        log.info("Fetching approved kitchens");
        Page<Kitchen> page = kitchenRepository.findByApprovalStatusAndIsActiveTrue(
                Kitchen.ApprovalStatus.APPROVED, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Get all active kitchens (for admin)
     */
    public PagedResponse<KitchenDTO> getAllKitchens(Pageable pageable) {
        log.info("Fetching all active kitchens");
        Page<Kitchen> page = kitchenRepository.findByIsActiveTrue(pageable);
        return buildPagedResponse(page);
    }

    /**
     * Search kitchens by name
     */
    public PagedResponse<KitchenDTO> searchKitchens(String query, Pageable pageable) {
        log.info("Searching kitchens with query: {}", query);
        Page<Kitchen> page = kitchenRepository.searchApprovedKitchens(query, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Get kitchens by city
     */
    public PagedResponse<KitchenDTO> getKitchensByCity(String city, Pageable pageable) {
        log.info("Fetching kitchens in city: {}", city);
        Page<Kitchen> page = kitchenRepository.findByCityAndIsActiveTrue(city, pageable);
        return buildPagedResponse(page);
    }

    /**
     * Approve kitchen (Admin only)
     */
    public KitchenDTO approveKitchen(Long kitchenId) {
        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));

        kitchen.setApprovalStatus(Kitchen.ApprovalStatus.APPROVED);
        kitchen.setVerified(true);
        kitchen.setUpdatedAt(LocalDateTime.now());
        kitchen = kitchenRepository.save(kitchen);

        log.info("Kitchen approved with ID: {}", kitchenId);
        return mapToDTO(kitchen);
    }

    /**
     * Reject kitchen (Admin only)
     */
    public KitchenDTO rejectKitchen(Long kitchenId) {
        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));

        kitchen.setApprovalStatus(Kitchen.ApprovalStatus.REJECTED);
        kitchen.setUpdatedAt(LocalDateTime.now());
        kitchen = kitchenRepository.save(kitchen);

        log.info("Kitchen rejected with ID: {}", kitchenId);
        return mapToDTO(kitchen);
    }

    /**
     * Update kitchen profile
     */
    public KitchenDTO updateKitchen(Long kitchenId, KitchenRegistrationRequest request) {
        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));

        kitchen.setKitchenName(request.getKitchenName());
        kitchen.setDescription(request.getDescription());
        kitchen.setAddress(request.getAddress());
        kitchen.setCity(request.getCity());
        kitchen.setState(request.getState());
        kitchen.setPostalCode(request.getPostalCode());
        kitchen.setCountry(request.getCountry());
        kitchen.setLatitude(request.getLatitude());
        kitchen.setLongitude(request.getLongitude());
        kitchen.setDeliveryArea(request.getDeliveryArea());
        kitchen.setCuisineTypes(request.getCuisineTypes());
        kitchen.setOwnerContact(request.getOwnerContact());
        kitchen.setOwnerEmail(request.getOwnerEmail());
        kitchen.setAlternateContact(request.getAlternateContact());
        kitchen.setAlternateEmail(request.getAlternateEmail());
        kitchen.setUpdatedAt(LocalDateTime.now());

        kitchen = kitchenRepository.save(kitchen);
        log.info("Kitchen updated with ID: {}", kitchenId);
        return mapToDTO(kitchen);
    }

    /**
     * Update kitchen availability status
     * @param kitchenId kitchen ID
     * @param active 1 for active, 0 for inactive
     */
    public KitchenDTO updateKitchenStatus(Long kitchenId, int active) {
        if (active != 0 && active != 1) {
            throw new ApplicationException("INVALID_STATUS",
                    "Status must be 1 (active) or 0 (inactive)");
        }

        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));

        kitchen.setIsActive(active == 1);
        kitchen.setUpdatedAt(LocalDateTime.now());
        kitchen = kitchenRepository.save(kitchen);

        log.info("Kitchen status updated to {} for ID: {}", active == 1 ? "active" : "inactive", kitchenId);
        return mapToDTO(kitchen);
    }

    /**
     * Deactivate kitchen
     */
    public KitchenDTO deactivateKitchen(Long kitchenId) {
        Kitchen kitchen = kitchenRepository.findById(kitchenId)
                .orElseThrow(() -> new ApplicationException("KITCHEN_NOT_FOUND",
                        "Kitchen not found"));

        kitchen.setIsActive(false);
        kitchen.setUpdatedAt(LocalDateTime.now());
        kitchen = kitchenRepository.save(kitchen);

        log.info("Kitchen deactivated with ID: {}", kitchenId);
        return mapToDTO(kitchen);
    }

    /**
     * Map Kitchen entity to DTO
     */
    private KitchenDTO mapToDTO(Kitchen kitchen) {
        return KitchenDTO.builder()
                .id(kitchen.getId())
                .kitchenName(kitchen.getKitchenName())
                .ownerUserId(kitchen.getOwnerUserId())
                .description(kitchen.getDescription())
                .address(kitchen.getAddress())
                .city(kitchen.getCity())
                .state(kitchen.getState())
                .postalCode(kitchen.getPostalCode())
                .country(kitchen.getCountry())
                .latitude(kitchen.getLatitude())
                .longitude(kitchen.getLongitude())
                .deliveryArea(kitchen.getDeliveryArea())
                .cuisineTypes(kitchen.getCuisineTypes())
                .ownerContact(kitchen.getOwnerContact())
                .ownerEmail(kitchen.getOwnerEmail())
                .alternateContact(kitchen.getAlternateContact())
                .alternateEmail(kitchen.getAlternateEmail())
                .approvalStatus(kitchen.getApprovalStatus())
                .rating(kitchen.getRating())
                .totalOrders(kitchen.getTotalOrders())
                .isActive(kitchen.getIsActive())
                .verified(kitchen.getVerified())
                .createdAt(kitchen.getCreatedAt())
                .updatedAt(kitchen.getUpdatedAt())
                .build();
    }

    /**
     * Build paged response
     */
    private PagedResponse<KitchenDTO> buildPagedResponse(Page<Kitchen> page) {
        return PagedResponse.<KitchenDTO>builder()
                .content(page.getContent().stream().map(this::mapToDTO).toList())
                .pagination(PaginationMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}
