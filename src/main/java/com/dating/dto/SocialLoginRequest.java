package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SocialLoginRequest {
    @NotBlank private String provider;
    @NotBlank private String token;
    private String deviceInfo;
}
