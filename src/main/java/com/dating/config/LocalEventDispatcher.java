package com.dating.config;

import com.dating.event.MatchEventListener;
import com.dating.event.NotificationEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalEventDispatcher {

    private final MatchEventListener matchEventListener;
    private final NotificationEventListener notificationEventListener;

    @EventListener
    @SuppressWarnings("unchecked")
    public void dispatch(MockKafkaConfig.LocalEvent event) {
        log.info("LocalEvent received on topic '{}': {}", event.getTopic(), event.getData());
        if (event.getData() instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) event.getData();
            if ("match-events".equals(event.getTopic())) {
                try {
                    matchEventListener.handleMatchEvent(data);
                } catch (Exception e) {
                    log.error("Error handling local match event", e);
                }
            }
            try {
                notificationEventListener.handleEvent(data);
            } catch (Exception e) {
                log.error("Error handling local notification event", e);
            }
        }
    }
}
