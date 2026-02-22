package com.makanforyou.payment.dto;

import com.makanforyou.payment.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for Payment Service
 * Used for API request/response communication
 */

/**
 * PaymentRequestDTO - Request body for creating/updating payments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class PaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Integer orderId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Payment amount cannot exceed 999999.99")
    private BigDecimal paymentAmount;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    private String transactionId;
}

/**
 * PaymentResponseDTO - Response body for payment endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class PaymentResponseDTO {

    private Integer paymentId;
    private Integer orderId;
    private Integer userId;
    private BigDecimal paymentAmount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
    private BigDecimal refundAmount;
    private LocalDateTime refundDate;
    private String refundReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

/**
 * ProcessPaymentRequestDTO - Request body for processing payments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequestDTO {

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    private String transactionId;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
}

/**
 * RefundRequestDTO - Request body for refund operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDTO {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    private String refundReason;
}

/**
 * PaymentStatsDTO - Payment statistics response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatsDTO {

    private Integer userId;
    private Long totalPayments;
    private BigDecimal totalAmount;
    private Long completedPayments;
    private BigDecimal completedAmount;
    private Long pendingPayments;
    private BigDecimal pendingAmount;
    private Long failedPayments;
    private BigDecimal failedAmount;
    private Long refundedPayments;
    private BigDecimal refundedAmount;
    private LocalDateTime lastPaymentDate;
    private BigDecimal averagePaymentAmount;
}

/**
 * UpdatePaymentStatusDTO - Request body for updating payment status (Admin only)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentStatusDTO {

    @NotNull(message = "Status is required")
    private Payment.PaymentStatus status;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}

/**
 * PaymentErrorDTO - Error response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentErrorDTO {

    private String timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private String traceId;
}

/**
 * PaginatedPaymentResponseDTO - Paginated list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedPaymentResponseDTO {

    private java.util.List<PaymentResponseDTO> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
}
