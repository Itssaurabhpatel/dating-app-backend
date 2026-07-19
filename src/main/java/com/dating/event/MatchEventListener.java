package com.dating.event;

import com.dating.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventListener {

    private final ChatService chatService;

    @KafkaListener(topics = "match-events", groupId = "chat-service")
    public void handleMatchEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        if ("MATCH_CREATED".equals(eventType)) {
            String matchId = (String) event.get("matchId");
            String user1Id = (String) event.get("user1Id");
            String user2Id = (String) event.get("user2Id");
            chatService.createChatRoom(matchId, user1Id, user2Id);
            log.info("Chat room created for match: {}", matchId);
        }
    }
}
