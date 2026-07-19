package com.dating.websocket;

import com.dating.dto.WebSocketMessage;
import com.dating.service.ChatService;
import com.dating.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        if (token != null && !jwtUtil.isTokenExpired(token)) {
            String userId = jwtUtil.extractUserId(token);
            sessions.put(userId, session);
            session.getAttributes().put("userId", userId);
            log.info("WebSocket connected for user: {}", userId);
        } else {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return;

        WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);

        switch (wsMessage.getType()) {
            case "TYPING" -> chatService.handleTyping(userId, wsMessage.getMatchId(), wsMessage.getIsTyping());
            case "READ_RECEIPT" -> chatService.markAsRead(userId, wsMessage.getMatchId());
            case "CALL_OFFER", "CALL_ANSWER", "ICE_CANDIDATE", "CALL_END" -> {
                String recipientId = wsMessage.getRecipientId();
                if (recipientId != null && sessions.containsKey(recipientId)) {
                    wsMessage.setSenderId(userId); // Ensure senderId is set correctly
                    try {
                        String payload = objectMapper.writeValueAsString(wsMessage);
                        sessions.get(recipientId).sendMessage(new TextMessage(payload));
                    } catch (Exception e) {
                        log.error("Failed to send WebRTC signaling message to {}", recipientId, e);
                    }
                } else {
                    log.warn("Recipient {} not found or offline for WebRTC message", recipientId);
                }
            }
            case "MESSAGE" -> {
                // If it's a direct message via websocket (though usually via REST in this app)
                String recipientId = wsMessage.getRecipientId();
                if (recipientId != null && sessions.containsKey(recipientId)) {
                    wsMessage.setSenderId(userId);
                    try {
                        sessions.get(recipientId).sendMessage(new TextMessage(objectMapper.writeValueAsString(wsMessage)));
                    } catch (Exception e) {
                        log.error("Failed to send message to {}", recipientId, e);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket disconnected for user: {}", userId);
        }
    }

    private String extractToken(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("token=")) {
            return query.substring(6);
        }
        return null;
    }
}
