package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_report_reporter", columnList = "reporter_id"),
    @Index(name = "idx_report_reported", columnList = "reported_user_id"),
    @Index(name = "idx_report_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report extends BaseEntity {
    @Column(name = "reporter_id", nullable = false, length = 36)
    private String reporterId;
    @Column(name = "reported_user_id", nullable = false, length = 36)
    private String reportedUserId;
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private ReportReason reason;
    @Column(name = "description", length = 1000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;
    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;
    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;
    @Column(name = "match_id", length = 36)
    private String matchId;
    @Column(name = "message_id", length = 36)
    private String messageId;

    public enum ReportReason {
        FAKE_PROFILE, INAPPROPRIATE_CONTENT, HARASSMENT, SPAM,
        UNDERAGE, OFFENSIVE_LANGUAGE, SCAM, OTHER
    }
    public enum ReportStatus { PENDING, REVIEWING, RESOLVED, DISMISSED }
}
