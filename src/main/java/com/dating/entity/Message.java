package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_msg_match", columnList = "match_id"),
    @Index(name = "idx_msg_sender", columnList = "sender_id"),
    @Index(name = "idx_msg_created", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message extends BaseEntity {
    @Column(name = "match_id", nullable = false, length = 36)
    private String matchId;
    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;
    @Column(name = "content", nullable = false, length = 2000)
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    @Column(name = "media_url", length = 500)
    private String mediaUrl;
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    @Column(name = "read_at")
    private Instant readAt;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    @Column(name = "is_reported", nullable = false)
    @Builder.Default
    private Boolean isReported = false;

    public enum MessageType { TEXT, IMAGE, AUDIO, VIDEO, GIF, LOCATION, STICKER }
}
