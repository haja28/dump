package com.makanforyou.delivery.dto;

import com.makanforyou.delivery.entity.Delivery;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for Delivery Service
 * Used for API request/response communication
 */

/**
 * DeliveryRequestDTO - Request body for creating deliveries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class DeliveryRequestDTO {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Integer orderId;

    @NotNull(message = "Kitchen ID is required")
    @Positive(message = "Kitchen ID must be positive")
    private Integer kitchenId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;

    @NotNull(message = "Item ID is required")
    @Positive(message = "Item ID must be positive")
    private Integer itemId;

    @NotNull(message = "Estimated delivery time is required")
    @FutureOrPresent(message = "Estimated delivery time must be in future")
    private LocalDateTime estimatedDeliveryTime;

    @Size(max = 500, message = "Delivery notes cannot exceed 500 characters")
    private String deliveryNotes;
}

/**
 * DeliveryResponseDTO - Response body for delivery endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class DeliveryResponseDTO {

    private Integer deliveryId;
    private Integer orderId;
    private Integer kitchenId;
    private Integer userId;
    private Integer itemId;
    private Delivery.DeliveryStatus deliveryStatus;
    private String assignedTo;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedDeliveryTime;
    private String currentLocation;
    private String deliveryNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

/**
 * AssignDeliveryRequestDTO - Request body for assigning delivery partner
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDeliveryRequestDTO {

    @NotBlank(message = "Partner name is required")
    @Size(max = 100, message = "Partner name cannot exceed 100 characters")
    private String partnerName;

    @NotBlank(message = "Partner phone is required")
    @Size(max = 15, message = "Partner phone cannot exceed 15 characters")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Partner phone contains invalid characters")
    private String partnerPhone;
}

/**
 * UpdateDeliveryStatusRequestDTO - Request body for updating delivery status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDeliveryStatusRequestDTO {

    @NotNull(message = "Update time is required")
    private LocalDateTime updateTime;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String currentLocation;
}

/**
 * CompleteDeliveryRequestDTO - Request body for completing delivery
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteDeliveryRequestDTO {

    @NotNull(message = "Delivery time is required")
    private LocalDateTime deliveryTime;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 100, message = "Recipient name cannot exceed 100 characters")
    private String recipientName;
}

/**
 * FailDeliveryRequestDTO - Request body for marking delivery as failed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailDeliveryRequestDTO {

    @NotBlank(message = "Failure reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}

/**
 * DeliveryPartnerStatsDTO - Delivery partner statistics response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerStatsDTO {

    private String partnerName;
    private Long totalDeliveries;
    private Long completedDeliveries;
    private Long failedDeliveries;
    private Long pendingDeliveries;
    private String averageDeliveryTime;  // Duration format: PT45M30S
    private Double successRate;          // Percentage: 95.45
    private Double totalDistance;        // In kilometers
    private Double topRating;            // 1-5 stars
    private LocalDateTime lastDeliveryDate;
}

/**
 * DeliveryStatsDTO - Overall delivery statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatsDTO {

    private Long totalDeliveries;
    private Long completedDeliveries;
    private Long failedDeliveries;
    private Long pendingDeliveries;
    private String averageDeliveryTime;  // Duration format: PT50M15S
    private Double successRate;          // Percentage: 96.0
    private Long onTimeDeliveries;
    private Long lateDeliveries;
    private Double averageRating;        // 1-5 stars
    private String topPartner;
    private String bottleneckLocation;
}

/**
 * DeliveryErrorDTO - Error response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryErrorDTO {

    private String timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private String traceId;
}

/**
 * PaginatedDeliveryResponseDTO - Paginated list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedDeliveryResponseDTO {

    private java.util.List<DeliveryResponseDTO> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
}
