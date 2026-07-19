package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog extends BaseEntity {
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    @Column(name = "body", nullable = false, length = 500)
    private String body;
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    @Column(name = "data", length = 1000)
    private String data;
    @Column(name = "is_sent", nullable = false)
    @Builder.Default
    private Boolean isSent = false;
    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
