package com.makanforyou.delivery.controller;

import com.makanforyou.delivery.dto.*;
import com.makanforyou.delivery.entity.Delivery;
import com.makanforyou.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Delivery Service REST Controller
 *
 * Base URL: http://localhost:8086/api/v1/deliveries
 * Authentication: JWT Bearer Token required for all endpoints
 *
 * Endpoints for managing delivery operations including:
 * - Delivery creation and tracking
 * - Status updates (assign, pickup, in-transit, complete)
 * - Delivery failure handling
 * - Partner and statistics management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // ============================================================
    // CREATE DELIVERY
    // ============================================================

    /**
     * POST /api/v1/deliveries
     * Create a new delivery record for an order
     * Called after order confirmation and payment processing
     *
     * @param requestDTO Delivery creation request with order and location details
     * @return 201 Created with delivery details
     * @throws IllegalArgumentException if order, kitchen, user, or item not found
     * @throws IllegalStateException if delivery already exists for order
     */
    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> createDelivery(@Valid @RequestBody DeliveryRequestDTO requestDTO) {
        log.info("Creating delivery for order ID: {}", requestDTO.getOrderId());

        DeliveryResponseDTO response = deliveryService.createDelivery(requestDTO);

        log.info("Delivery created successfully with ID: {}", response.getDeliveryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // GET DELIVERY BY ID
    // ============================================================

    /**
     * GET /api/v1/deliveries/{deliveryId}
     * Retrieve delivery details by delivery ID
     * Provides complete tracking information
     *
     * @param deliveryId Delivery identifier
     * @return 200 OK with delivery details
     * @throws DeliveryNotFoundException if delivery not found
     */
    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(@PathVariable Integer deliveryId) {
        log.info("Fetching delivery with ID: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.getDeliveryById(deliveryId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET DELIVERY BY ORDER ID
    // ============================================================

    /**
     * GET /api/v1/deliveries/order/{orderId}
     * Retrieve delivery details using order ID
     * Useful for checking delivery status after order placement
     *
     * @param orderId Order identifier
     * @return 200 OK with delivery details
     * @throws DeliveryNotFoundException if delivery not found for order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryByOrderId(@PathVariable Integer orderId) {
        log.info("Fetching delivery for order ID: {}", orderId);

        DeliveryResponseDTO response = deliveryService.getDeliveryByOrderId(orderId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ASSIGN DELIVERY PARTNER
    // ============================================================

    /**
     * PUT /api/v1/deliveries/{deliveryId}/assign
     * Assign a delivery partner to a delivery
     * Transitions status from PENDING to ASSIGNED
     *
     * @param deliveryId Delivery identifier
     * @param requestDTO Assignment request with partner details
     * @return 200 OK with updated delivery details
     * @throws DeliveryNotFoundException if delivery not found
     * @throws IllegalStateException if delivery not in PENDING status
     */
    @PutMapping("/{deliveryId}/assign")
    public ResponseEntity<DeliveryResponseDTO> assignDeliveryPartner(
            @PathVariable Integer deliveryId,
            @Valid @RequestBody AssignDeliveryRequestDTO requestDTO) {
        log.info("Assigning delivery partner to delivery ID: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.assignDeliveryPartner(deliveryId, requestDTO);

        log.info("Delivery partner assigned successfully: {}", deliveryId);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE PICKUP STATUS
    // ============================================================

    /**
     * PUT /api/v1/deliveries/{deliveryId}/pickup
     * Mark delivery as picked up from kitchen
     * Transitions status from ASSIGNED to PICKED_UP
     *
     * @param deliveryId Delivery identifier
     * @param requestDTO Pickup update request with timestamp
     * @return 200 OK with updated delivery details
     * @throws DeliveryNotFoundException if delivery not found
     * @throws IllegalStateException if delivery not in ASSIGNED status
     */
    @PutMapping("/{deliveryId}/pickup")
    public ResponseEntity<DeliveryResponseDTO> updatePickupStatus(
            @PathVariable Integer deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequestDTO requestDTO) {
        log.info("Marking delivery as picked up: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.updatePickupStatus(deliveryId, requestDTO);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE IN-TRANSIT STATUS
    // ============================================================

    /**
     * PUT /api/v1/deliveries/{deliveryId}/in-transit
     * Mark delivery as in transit and update current location
     * Transitions status from PICKED_UP to IN_TRANSIT
     * Called periodically as delivery progresses
     *
     * @param deliveryId Delivery identifier
     * @param requestDTO In-transit update request with location
     * @return 200 OK with updated delivery details
     * @throws DeliveryNotFoundException if delivery not found
     * @throws IllegalStateException if delivery not in PICKED_UP status
     */
    @PutMapping("/{deliveryId}/in-transit")
    public ResponseEntity<DeliveryResponseDTO> updateInTransitStatus(
            @PathVariable Integer deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequestDTO requestDTO) {
        log.info("Marking delivery as in transit: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.updateInTransitStatus(deliveryId, requestDTO);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // COMPLETE DELIVERY
    // ============================================================

    /**
     * PUT /api/v1/deliveries/{deliveryId}/complete
     * Mark delivery as complete (successfully delivered)
     * Transitions status from IN_TRANSIT to DELIVERED
     * Terminal state for successful delivery
     *
     * @param deliveryId Delivery identifier
     * @param requestDTO Completion request with delivery time
     * @return 200 OK with updated delivery details
     * @throws DeliveryNotFoundException if delivery not found
     * @throws IllegalStateException if delivery not in IN_TRANSIT status
     */
    @PutMapping("/{deliveryId}/complete")
    public ResponseEntity<DeliveryResponseDTO> completeDelivery(
            @PathVariable Integer deliveryId,
            @Valid @RequestBody CompleteDeliveryRequestDTO requestDTO) {
        log.info("Marking delivery as complete: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.completeDelivery(deliveryId, requestDTO);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // MARK DELIVERY AS FAILED
    // ============================================================

    /**
     * PUT /api/v1/deliveries/{deliveryId}/failed
     * Mark delivery as failed and record failure reason
     * Can be called from any status (for retry/manual intervention)
     *
     * @param deliveryId Delivery identifier
     * @param requestDTO Failure request with reason
     * @return 200 OK with updated delivery details
     * @throws DeliveryNotFoundException if delivery not found
     */
    @PutMapping("/{deliveryId}/failed")
    public ResponseEntity<DeliveryResponseDTO> markDeliveryFailed(
            @PathVariable Integer deliveryId,
            @Valid @RequestBody FailDeliveryRequestDTO requestDTO) {
        log.info("Marking delivery as failed: {}", deliveryId);

        DeliveryResponseDTO response = deliveryService.markDeliveryFailed(deliveryId, requestDTO);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // LIST KITCHEN DELIVERIES
    // ============================================================

    /**
     * GET /api/v1/deliveries/kitchen/{kitchenId}
     * List all deliveries for a kitchen with pagination and filtering
     * Kitchen staff can view orders being delivered from their kitchen
     *
     * Query Parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - status: Filter by delivery status
     * - date: Filter by specific date (YYYY-MM-DD format)
     * - sortOrder: asc or desc (default: desc)
     *
     * @param kitchenId Kitchen identifier
     * @param page Page number
     * @param size Page size
     * @param status Optional status filter
     * @param date Optional date filter
     * @param sortOrder Sort order
     * @return 200 OK with paginated delivery list
     */
    @GetMapping("/kitchen/{kitchenId}")
    public ResponseEntity<PaginatedDeliveryResponseDTO> listKitchenDeliveries(
            @PathVariable Integer kitchenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Delivery.DeliveryStatus status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        log.info("Fetching deliveries for kitchen ID: {}, page: {}, size: {}", kitchenId, page, size);

        LocalDate filterDate = null;
        if (date != null) {
            filterDate = LocalDate.parse(date, DATE_FORMATTER);
        }

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortOrder, "createdAt"));
        Page<DeliveryResponseDTO> response = deliveryService.listKitchenDeliveries(kitchenId, status, filterDate, pageable);

        return ResponseEntity.ok(PaginatedDeliveryResponseDTO.builder()
                .content(response.getContent())
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .currentPage(response.getNumber())
                .pageSize(response.getSize())
                .hasNext(response.hasNext())
                .hasPrevious(response.hasPrevious())
                .build());
    }

    // ============================================================
    // LIST USER DELIVERIES
    // ============================================================

    /**
     * GET /api/v1/deliveries/user/{userId}
     * List all deliveries for a customer
     * Customers can track their order deliveries
     *
     * Query Parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - status: Filter by delivery status
     * - sortBy: Field to sort by (createdAt, deliveryTime)
     * - sortOrder: asc or desc (default: desc)
     *
     * @param userId User identifier
     * @param page Page number
     * @param size Page size
     * @param status Optional status filter
     * @param sortBy Sort field
     * @param sortOrder Sort order
     * @return 200 OK with paginated delivery list
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedDeliveryResponseDTO> listUserDeliveries(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Delivery.DeliveryStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        log.info("Fetching deliveries for user ID: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortOrder, sortBy));
        Page<DeliveryResponseDTO> response = deliveryService.listUserDeliveries(userId, status, pageable);

        return ResponseEntity.ok(PaginatedDeliveryResponseDTO.builder()
                .content(response.getContent())
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .currentPage(response.getNumber())
                .pageSize(response.getSize())
                .hasNext(response.hasNext())
                .hasPrevious(response.hasPrevious())
                .build());
    }

    // ============================================================
    // DELIVERY PARTNER STATISTICS
    // ============================================================

    /**
     * GET /api/v1/deliveries/partner/{partnerName}/stats
     * Get statistics for a delivery partner
     * Admin view for delivery performance analysis
     *
     * Query Parameters:
     * - startDate: Start date for statistics (YYYY-MM-DD format, optional)
     * - endDate: End date for statistics (YYYY-MM-DD format, optional)
     *
     * @param partnerName Delivery partner name
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return 200 OK with partner statistics
     */
    @GetMapping("/partner/{partnerName}/stats")
    public ResponseEntity<DeliveryPartnerStatsDTO> getPartnerStats(
            @PathVariable String partnerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Fetching delivery stats for partner: {}", partnerName);

        LocalDate start = null, end = null;
        if (startDate != null) {
            start = LocalDate.parse(startDate, DATE_FORMATTER);
        }
        if (endDate != null) {
            end = LocalDate.parse(endDate, DATE_FORMATTER);
        }

        DeliveryPartnerStatsDTO response = deliveryService.getPartnerStats(partnerName, start, end);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // OVERALL DELIVERY STATISTICS
    // ============================================================

    /**
     * GET /api/v1/deliveries/stats
     * Get overall delivery statistics (Admin only)
     * System-wide delivery performance metrics
     *
     * Query Parameters:
     * - startDate: Start date for statistics (YYYY-MM-DD format, optional)
     * - endDate: End date for statistics (YYYY-MM-DD format, optional)
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return 200 OK with overall statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DeliveryStatsDTO> getOverallStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Fetching overall delivery statistics");

        LocalDate start = null, end = null;
        if (startDate != null) {
            start = LocalDate.parse(startDate, DATE_FORMATTER);
        }
        if (endDate != null) {
            end = LocalDate.parse(endDate, DATE_FORMATTER);
        }

        DeliveryStatsDTO response = deliveryService.getOverallStats(start, end);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ERROR HANDLING
    // ============================================================

    /**
     * Handle DeliveryNotFoundException
     */
    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<DeliveryErrorDTO> handleDeliveryNotFound(DeliveryNotFoundException ex) {
        log.error("Delivery not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DeliveryErrorDTO.builder()
                        .timestamp(java.time.Instant.now().toString())
                        .status(404)
                        .error("Not Found")
                        .message(ex.getMessage())
                        .build());
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DeliveryErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(DeliveryErrorDTO.builder()
                        .timestamp(java.time.Instant.now().toString())
                        .status(400)
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .build());
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<DeliveryErrorDTO> handleIllegalState(IllegalStateException ex) {
        log.error("Invalid state: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DeliveryErrorDTO.builder()
                        .timestamp(java.time.Instant.now().toString())
                        .status(409)
                        .error("Conflict")
                        .message(ex.getMessage())
                        .build());
    }
}

/**
 * DeliveryNotFoundException
 * Thrown when a delivery record cannot be found
 */
class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(String message) {
        super(message);
    }
}
