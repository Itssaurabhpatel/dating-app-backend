package com.dating.event;

import com.dating.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = {"match-events", "like-events", "notification-events"}, groupId = "notification-service")
    public void handleEvent(Map<String, Object> event) {
        log.info("Received event: {}", event);
        notificationService.handleEvent(event);
    }
}
