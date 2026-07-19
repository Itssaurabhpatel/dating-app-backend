package com.dating.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MatchResponse {
    private String id;
    private String userId;
    private String name;
    private String profilePhotoUrl;
    private Instant matchedAt;
    private Instant lastMessageAt;
    private String lastMessage;
}
