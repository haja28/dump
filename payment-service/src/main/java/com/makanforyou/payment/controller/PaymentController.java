package com.makanforyou.payment.controller;

import com.makanforyou.payment.dto.*;
import com.makanforyou.payment.entity.Payment;
import com.makanforyou.payment.service.PaymentService;
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

/**
 * Payment Service REST Controller
 *
 * Base URL: http://localhost:8085/api/v1/payments
 * Authentication: JWT Bearer Token required for all endpoints
 *
 * Endpoints for managing payment operations including:
 * - Payment creation and retrieval
 * - Payment processing and status updates
 * - Refund management
 * - Payment statistics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ============================================================
    // CREATE PAYMENT
    // ============================================================

    /**
     * POST /api/v1/payments
     * Create a new payment record for an order
     *
     * @param requestDTO Payment creation request with order and amount details
     * @return 201 Created with payment details
     * @throws IllegalArgumentException if order or user not found
     * @throws IllegalStateException if payment already exists for order
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        log.info("Creating payment for order ID: {}", requestDTO.getOrderId());

        PaymentResponseDTO response = paymentService.createPayment(requestDTO);

        log.info("Payment created successfully with ID: {}", response.getPaymentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // GET PAYMENT BY ID
    // ============================================================

    /**
     * GET /api/v1/payments/{paymentId}
     * Retrieve payment details by payment ID
     *
     * @param paymentId Payment identifier
     * @return 200 OK with payment details
     * @throws PaymentNotFoundException if payment not found
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Integer paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);

        PaymentResponseDTO response = paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET PAYMENT BY ORDER ID
    // ============================================================

    /**
     * GET /api/v1/payments/order/{orderId}
     * Retrieve payment details using order ID
     * Useful for checking payment status after order creation
     *
     * @param orderId Order identifier
     * @return 200 OK with payment details
     * @throws PaymentNotFoundException if payment not found for order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable Integer orderId) {
        log.info("Fetching payment for order ID: {}", orderId);

        PaymentResponseDTO response = paymentService.getPaymentByOrderId(orderId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // PROCESS PAYMENT
    // ============================================================

    /**
     * PUT /api/v1/payments/{paymentId}/process
     * Process payment and update status to COMPLETED
     * Gateway integration point for payment confirmation
     *
     * @param paymentId Payment identifier
     * @param requestDTO Process request with transaction details
     * @return 200 OK with updated payment details
     * @throws PaymentNotFoundException if payment not found
     * @throws IllegalStateException if payment already processed
     */
    @PutMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @PathVariable Integer paymentId,
            @Valid @RequestBody ProcessPaymentRequestDTO requestDTO) {
        log.info("Processing payment with ID: {}", paymentId);

        PaymentResponseDTO response = paymentService.processPayment(paymentId, requestDTO);

        log.info("Payment processed successfully: {}", paymentId);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // REFUND PAYMENT
    // ============================================================

    /**
     * PUT /api/v1/payments/{paymentId}/refund
     * Initiate refund for a completed payment
     * Updates payment status to REFUNDED and records refund details
     *
     * @param paymentId Payment identifier
     * @param requestDTO Refund request with amount and reason
     * @return 200 OK with refunded payment details
     * @throws PaymentNotFoundException if payment not found
     * @throws IllegalStateException if payment not in COMPLETED status
     * @throws IllegalArgumentException if refund amount exceeds payment amount
     */
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @PathVariable Integer paymentId,
            @Valid @RequestBody RefundRequestDTO requestDTO) {
        log.info("Refunding payment with ID: {}", paymentId);

        PaymentResponseDTO response = paymentService.refundPayment(paymentId, requestDTO);

        log.info("Payment refunded successfully: {}", paymentId);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // LIST USER PAYMENTS
    // ============================================================

    /**
     * GET /api/v1/payments/user/{userId}
     * List all payments for a user with pagination and filtering
     *
     * Query Parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - status: Filter by payment status (PENDING, COMPLETED, FAILED, REFUNDED)
     * - sortBy: Field to sort by (paymentDate, createdAt)
     * - sortOrder: asc or desc (default: desc)
     *
     * @param userId User identifier
     * @param page Page number
     * @param size Page size
     * @param status Optional status filter
     * @param sortBy Sort field
     * @param sortOrder Sort order
     * @return 200 OK with paginated payment list
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedPaymentResponseDTO> listUserPayments(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        log.info("Fetching payments for user ID: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortOrder, sortBy));
        Page<PaymentResponseDTO> response = paymentService.listUserPayments(userId, status, pageable);

        return ResponseEntity.ok(PaginatedPaymentResponseDTO.builder()
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
    // PAYMENT STATISTICS
    // ============================================================

    /**
     * GET /api/v1/payments/stats/user/{userId}
     * Get comprehensive payment statistics for a user
     * Includes totals, counts by status, and averages
     *
     * @param userId User identifier
     * @return 200 OK with payment statistics
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<PaymentStatsDTO> getPaymentStats(@PathVariable Integer userId) {
        log.info("Fetching payment statistics for user ID: {}", userId);

        PaymentStatsDTO response = paymentService.getPaymentStats(userId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE PAYMENT STATUS (ADMIN ONLY)
    // ============================================================

    /**
     * PATCH /api/v1/payments/{paymentId}/status
     * Update payment status directly (Admin only)
     * Used for manual interventions and corrections
     *
     * Requires ADMIN role
     *
     * @param paymentId Payment identifier
     * @param requestDTO Status update request
     * @return 200 OK with updated payment details
     * @throws PaymentNotFoundException if payment not found
     * @throws IllegalArgumentException if invalid status transition
     */
    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable Integer paymentId,
            @Valid @RequestBody UpdatePaymentStatusDTO requestDTO) {
        log.info("Updating payment status for ID: {} to {}", paymentId, requestDTO.getStatus());

        PaymentResponseDTO response = paymentService.updatePaymentStatus(paymentId, requestDTO);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ERROR HANDLING
    // ============================================================

    /**
     * Handle PaymentNotFoundException
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<PaymentErrorDTO> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.error("Payment not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(PaymentErrorDTO.builder()
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
    public ResponseEntity<PaymentErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(PaymentErrorDTO.builder()
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
    public ResponseEntity<PaymentErrorDTO> handleIllegalState(IllegalStateException ex) {
        log.error("Invalid state: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(PaymentErrorDTO.builder()
                        .timestamp(java.time.Instant.now().toString())
                        .status(409)
                        .error("Conflict")
                        .message(ex.getMessage())
                        .build());
    }
}

/**
 * PaymentNotFoundException
 * Thrown when a payment record cannot be found
 */
class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
