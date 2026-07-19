package com.dating.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatRoomResponse {
    private String matchId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserPhoto;
    private String lastMessage;
    private Instant lastMessageAt;
    private Integer unreadCount;
    private Boolean isActive;
}
