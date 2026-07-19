package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_chat_match", columnList = "match_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom extends BaseEntity {
    @Column(name = "match_id", nullable = false, unique = true, length = 36)
    private String matchId;
    @Column(name = "user1_id", nullable = false, length = 36)
    private String user1Id;
    @Column(name = "user2_id", nullable = false, length = 36)
    private String user2Id;
    @Column(name = "last_message")
    private String lastMessage;
    @Column(name = "last_message_at")
    private Instant lastMessageAt;
    @Column(name = "user1_unread", nullable = false)
    @Builder.Default
    private Integer user1Unread = 0;
    @Column(name = "user2_unread", nullable = false)
    @Builder.Default
    private Integer user2Unread = 0;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
