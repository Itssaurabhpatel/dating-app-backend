package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_tokens", indexes = {
    @Index(name = "idx_device_user", columnList = "user_id"),
    @Index(name = "idx_device_token", columnList = "token", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceToken extends BaseEntity {
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;
    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType; // ANDROID, IOS, WEB
    @Column(name = "device_info", length = 255)
    private String deviceInfo;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
