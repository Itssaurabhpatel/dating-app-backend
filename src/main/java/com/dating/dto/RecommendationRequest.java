package com.dating.dto;

import lombok.Data;

@Data
public class RecommendationRequest {
    private Integer minAge = 18;
    private Integer maxAge = 100;
    private Integer maxDistance = 50;
    private String gender;
    private java.util.List<String> interests;
    private Boolean verifiedOnly = false;
    private Boolean premiumOnly = false;
}
