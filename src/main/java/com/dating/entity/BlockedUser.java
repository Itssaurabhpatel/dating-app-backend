package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blocked_users", indexes = {
    @Index(name = "idx_block_user", columnList = "user_id"),
    @Index(name = "idx_block_pair", columnList = "user_id,blocked_user_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlockedUser extends BaseEntity {
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "blocked_user_id", nullable = false, length = 36)
    private String blockedUserId;
    @Column(name = "reason", length = 255)
    private String reason;
}
