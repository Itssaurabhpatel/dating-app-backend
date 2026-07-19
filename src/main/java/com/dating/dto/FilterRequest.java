package com.dating.dto;

import java.util.List;
import lombok.Data;

@Data
public class FilterRequest {
    private Integer minAge = 18;
    private Integer maxAge = 100;
    private Integer maxDistance = 50; // km
    private String gender;
    private List<String> interests;
    private Boolean verifiedOnly = false;
}
