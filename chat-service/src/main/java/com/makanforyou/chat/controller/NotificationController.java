package com.makanforyou.chat.controller;

import com.makanforyou.chat.dto.NotificationDTO;
import com.makanforyou.chat.entity.Message;
import com.makanforyou.chat.service.NotificationService;
import com.makanforyou.common.dto.ApiResponse;
import com.makanforyou.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for notification management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Chat notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications
     * GET /api/v1/notifications
     */
    @GetMapping
    @Operation(summary = "Get notifications", description = "Get all notifications for a user")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDTO>>> getNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching notifications for {} {}", userType, userId);

        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<NotificationDTO> response = notificationService.getNotifications(userId, senderType, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notifications
     * GET /api/v1/notifications/unread
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get unread notifications for a user")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDTO>>> getUnreadNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("Fetching unread notifications for {} {}", userType, userId);

        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<NotificationDTO> response = notificationService.getUnreadNotifications(userId, senderType, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notification count
     * GET /api/v1/notifications/unread/count
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        Long count = notificationService.countUnreadNotifications(userId, senderType);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Mark notification as read
     * PUT /api/v1/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        log.info("Marking notification {} as read", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     * PUT /api/v1/notifications/read-all
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Marking all notifications as read for {} {}", userType, userId);

        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        notificationService.markAllAsRead(userId, senderType);

        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    /**
     * Clear all notifications
     * DELETE /api/v1/notifications
     */
    @DeleteMapping
    @Operation(summary = "Clear notifications", description = "Clear all notifications for a user")
    public ResponseEntity<ApiResponse<Void>> clearAllNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Type", defaultValue = "CUSTOMER") String userType) {
        log.info("Clearing all notifications for {} {}", userType, userId);

        Message.SenderType senderType = "CUSTOMER".equalsIgnoreCase(userType) ?
                Message.SenderType.CUSTOMER : Message.SenderType.KITCHEN;
        notificationService.clearAllNotifications(userId, senderType);

        return ResponseEntity.ok(ApiResponse.success(null, "All notifications cleared"));
    }
}
