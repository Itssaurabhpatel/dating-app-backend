package com.dating.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WebSocketMessage {
    private String type; // MESSAGE, TYPING, READ_RECEIPT, ONLINE_STATUS, CALL_OFFER, CALL_ANSWER, ICE_CANDIDATE, CALL_END
    private String matchId;
    private String senderId;
    private String recipientId; // For directing signaling to a specific user
    private String content;
    private String messageType;
    private String mediaUrl;
    private Long timestamp;
    private Boolean isTyping;
    private Boolean isOnline;
    
    // WebRTC signaling fields
    private Map<String, Object> sdp;
    private Map<String, Object> candidate;
}
