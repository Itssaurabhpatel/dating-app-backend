package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterestRequest {
    @NotBlank private String name;
    private String icon;
    private String category;
}
