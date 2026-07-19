package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SwipeRequest {
    @NotBlank private String targetId;
    @NotBlank private String direction; // LEFT, RIGHT
    private Boolean isSuperLike;
}
