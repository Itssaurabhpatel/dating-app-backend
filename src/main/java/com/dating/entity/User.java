package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {
    @Column(name = "email", unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "interested_in", length = 20)
    private Gender interestedIn;
    @Column(name = "bio", length = 500)
    private String bio;
    @Column(name = "occupation", length = 100)
    private String occupation;
    @Column(name = "education", length = 100)
    private String education;
    @Column(name = "height_cm")
    private Integer heightCm;
    @Column(name = "religion", length = 50)
    private String religion;
    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "language", length = 50)
    private Set<String> languages = new HashSet<>();
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "country", length = 100)
    private String country;
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;
    @Column(name = "profile_completion", nullable = false)
    @Builder.Default
    private Integer profileCompletion = 0;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    @Column(name = "selfie_verified", nullable = false)
    @Builder.Default
    private Boolean selfieVerified = false;
    @Column(name = "id_verified", nullable = false)
    @Builder.Default
    private Boolean idVerified = false;
    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;
    @Column(name = "premium_expiry")
    private Instant premiumExpiry;
    @Column(name = "last_active")
    private Instant lastActive;
    @Column(name = "deleted_at")
    private Instant deletedAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @Column(name = "google_id", length = 255)
    private String googleId;
    @Column(name = "apple_id", length = 255)
    private String appleId;
    @Column(name = "otp_code", length = 10)
    private String otpCode;
    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    public enum Gender { MALE, FEMALE, NON_BINARY, OTHER }
    public enum UserStatus { ACTIVE, SUSPENDED, DEACTIVATED, DELETED }
    public enum Role { ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR }
}
