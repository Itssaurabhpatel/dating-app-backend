package com.dating.dto;

import com.dating.entity.Profile;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class ProfileResponse {
    private String id;
    private String userId;
    private String name;
    private LocalDate dateOfBirth;
    private Integer age;
    private Profile.Gender gender;
    private Profile.Gender interestedIn;
    private String bio;
    private String occupation;
    private String education;
    private Integer heightCm;
    private String religion;
    private Set<String> languages;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;
    private Integer profileCompletion;
    private Boolean isVisible;
    private List<PhotoResponse> photos;
    private List<String> interests;
    private Instant lastActive;
    private Instant createdAt;
}
