package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token", columnList = "token"),
    @Index(name = "idx_refresh_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken extends BaseEntity {
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;
    @Column(name = "device_info", length = 255)
    private String deviceInfo;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
