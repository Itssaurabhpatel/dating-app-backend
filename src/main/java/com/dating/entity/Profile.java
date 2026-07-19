package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profiles", indexes = {
    @Index(name = "idx_profile_user", columnList = "user_id", unique = true),
    @Index(name = "idx_profile_gender", columnList = "gender"),
    @Index(name = "idx_profile_location", columnList = "latitude,longitude")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile extends BaseEntity {
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "interested_in", nullable = false, length = 20)
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
    @CollectionTable(name = "profile_languages", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "language", length = 50)
    private Set<String> languages = new HashSet<>();
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProfilePhoto> photos = new HashSet<>();
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "country", length = 100)
    private String country;
    @Column(name = "profile_completion", nullable = false)
    @Builder.Default
    private Integer profileCompletion = 0;
    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;
    @Column(name = "hide_age", nullable = false)
    @Builder.Default
    private Boolean hideAge = false;
    @Column(name = "hide_distance", nullable = false)
    @Builder.Default
    private Boolean hideDistance = false;
    @Column(name = "last_active")
    private java.time.Instant lastActive;

    public enum Gender { MALE, FEMALE, NON_BINARY, OTHER }
}
