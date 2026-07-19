package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moderation_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModerationLog extends BaseEntity {
    @Column(name = "moderator_id", nullable = false, length = 36)
    private String moderatorId;
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    @Column(name = "target_user_id", length = 36)
    private String targetUserId;
    @Column(name = "target_report_id", length = 36)
    private String targetReportId;
    @Column(name = "details", length = 2000)
    private String details;
}
