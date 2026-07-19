package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.entity.DeviceToken;
import com.dating.entity.NotificationLog;
import com.dating.repository.DeviceTokenRepository;
import com.dating.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public ApiResponse<Void> registerDeviceToken(String userId, DeviceTokenRequest request) {
        deviceTokenRepository.findByToken(request.getToken()).ifPresent(deviceTokenRepository::delete);
        DeviceToken token = DeviceToken.builder()
                .userId(userId)
                .token(request.getToken())
                .deviceType(request.getDeviceType())
                .deviceInfo(request.getDeviceInfo())
                .isActive(true)
                .build();
        deviceTokenRepository.save(token);
        return ApiResponse.success(null, "Device token registered");
    }

    @Transactional
    public ApiResponse<Void> unregisterDeviceToken(String userId, String token) {
        deviceTokenRepository.findByToken(token).ifPresent(t -> {
            if (t.getUserId().equals(userId)) {
                deviceTokenRepository.delete(t);
            }
        });
        return ApiResponse.success(null, "Device token unregistered");
    }

    @Transactional
    public void sendNotification(String userId, String title, String body, String type, Map<String, String> data) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
        if (tokens.isEmpty()) {
            log.warn("No active device tokens for user: {}", userId);
            return;
        }

        NotificationLog logEntry = NotificationLog.builder()
                .userId(userId).title(title).body(body).type(type)
                .data(data != null ? data.toString() : null)
                .build();

        for (DeviceToken token : tokens) {
            try {
                // TODO: Integrate with FCM SDK
                // sendToFcm(token.getToken(), title, body, data);
                log.info("Sending notification to {}: {}", userId, title);
            } catch (Exception e) {
                log.error("Failed to send notification: {}", e.getMessage());
                logEntry.setErrorMessage(e.getMessage());
            }
        }

        logEntry.setIsSent(true);
        notificationLogRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationLog>> getNotificationHistory(String userId, int page, int size) {
        return ApiResponse.success(
                notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId,
                        org.springframework.data.domain.PageRequest.of(page, size)).getContent());
    }

    @Transactional
    public void handleEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        switch (eventType) {
            case "MATCH_CREATED" -> {
                String user1 = (String) event.get("user1Id");
                String user2 = (String) event.get("user2Id");
                sendNotification(user2, "New Match!", "You have a new match!", "MATCH",
                        Map.of("matchId", (String) event.get("matchId")));
                sendNotification(user1, "New Match!", "You have a new match!", "MATCH",
                        Map.of("matchId", (String) event.get("matchId")));
            }
            case "PROFILE_LIKED" -> {
                String receiverId = (String) event.get("receiverId");
                boolean isSuperLike = Boolean.TRUE.equals(event.get("isSuperLike"));
                sendNotification(receiverId,
                        isSuperLike ? "Super Like!" : "New Like!",
                        isSuperLike ? "Someone super liked you!" : "Someone liked your profile!",
                        isSuperLike ? "SUPER_LIKE" : "LIKE", null);
            }
            case "NEW_MESSAGE" -> {
                String receiverId = (String) event.get("receiverId");
                String senderId = (String) event.get("senderId");
                String content = (String) event.get("content");
                sendNotification(receiverId, "New Message", content, "MESSAGE",
                        Map.of("matchId", (String) event.get("matchId"), "senderId", senderId));
            }
            case "SUBSCRIPTION_EXPIRED" -> {
                String userId = (String) event.get("userId");
                sendNotification(userId, "Subscription Expired", "Your premium subscription has expired.", "SUBSCRIPTION", null);
            }
        }
    }
}
