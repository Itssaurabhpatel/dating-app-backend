package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_user1", columnList = "user1_id"),
    @Index(name = "idx_match_user2", columnList = "user2_id"),
    @Index(name = "idx_match_pair", columnList = "user1_id,user2_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Match extends BaseEntity {
    @Column(name = "user1_id", nullable = false, length = 36)
    private String user1Id;
    @Column(name = "user2_id", nullable = false, length = 36)
    private String user2Id;
    @Column(name = "matched_at", nullable = false)
    private Instant matchedAt;
    @Column(name = "unmatched_at")
    private Instant unmatchedAt;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    @Column(name = "last_message_at")
    private Instant lastMessageAt;
}
