package com.dating.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationResponse {
    private String userId;
    private String name;
    private Integer age;
    private String gender;
    private String bio;
    private String occupation;
    private String education;
    private Integer heightCm;
    private String city;
    private String country;
    private Double distanceKm;
    private List<String> photos;
    private List<String> interests;
    private Integer profileCompletion;
    private Double compatibilityScore;
    private Boolean isPremium;
    private Boolean isVerified;
}
