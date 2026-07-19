package com.dating.entity;

import lombok.*;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TypingStatus {
    private String matchId;
    private String userId;
    private boolean isTyping;
    private Instant timestamp;
}
