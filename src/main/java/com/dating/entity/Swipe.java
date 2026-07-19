package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "swipes", indexes = {
    @Index(name = "idx_swipe_user", columnList = "user_id"),
    @Index(name = "idx_swipe_target", columnList = "target_id"),
    @Index(name = "idx_swipe_pair", columnList = "user_id,target_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Swipe extends BaseEntity {
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private SwipeDirection direction;
    @Column(name = "is_super_like", nullable = false)
    @Builder.Default
    private Boolean isSuperLike = false;

    public enum SwipeDirection { LEFT, RIGHT }
}
