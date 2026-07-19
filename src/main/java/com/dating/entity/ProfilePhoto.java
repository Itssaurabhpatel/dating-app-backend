package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_photos", indexes = {
    @Index(name = "idx_photo_profile", columnList = "profile_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfilePhoto extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}
