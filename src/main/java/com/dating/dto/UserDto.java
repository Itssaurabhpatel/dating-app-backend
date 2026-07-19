package com.dating.dto;

import com.dating.entity.User;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDto {
    private String id;
    private String email;
    private String name;
    private LocalDate dateOfBirth;
    private Integer age;
    private User.Gender gender;
    private User.Gender interestedIn;
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
    private String profilePhotoUrl;
    private Integer profileCompletion;
    private User.UserStatus status;
    private Boolean emailVerified;
    private Boolean selfieVerified;
    private Boolean idVerified;
    private Boolean isPremium;
    private Instant premiumExpiry;
    private Instant lastActive;
    private Instant createdAt;
}
