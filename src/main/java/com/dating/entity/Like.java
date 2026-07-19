package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes", indexes = {
    @Index(name = "idx_like_sender", columnList = "sender_id"),
    @Index(name = "idx_like_receiver", columnList = "receiver_id"),
    @Index(name = "idx_like_sender_receiver", columnList = "sender_id,receiver_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Like extends BaseEntity {
    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;
    @Column(name = "receiver_id", nullable = false, length = 36)
    private String receiverId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private LikeType type = LikeType.LIKE;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LikeStatus status = LikeStatus.PENDING;
    @Column(name = "is_super_like", nullable = false)
    @Builder.Default
    private Boolean isSuperLike = false;

    public enum LikeType { LIKE, PASS }
    public enum LikeStatus { PENDING, MATCHED, DECLINED }
}
