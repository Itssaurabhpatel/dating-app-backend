package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.entity.NotificationLog;
import com.dating.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Push notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    @Operation(summary = "Register device token for push notifications")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(notificationService.registerDeviceToken(userId, request));
    }

    @DeleteMapping("/token/{token}")
    @Operation(summary = "Unregister device token")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String token) {
        return ResponseEntity.ok(notificationService.unregisterDeviceToken(userId, token));
    }

    @GetMapping("/history")
    @Operation(summary = "Get notification history")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotificationHistory(userId, page, size));
    }
}
