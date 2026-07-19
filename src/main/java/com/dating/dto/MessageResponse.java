package com.dating.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MessageResponse {
    private String id;
    private String matchId;
    private String senderId;
    private String content;
    private String messageType;
    private String mediaUrl;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}
