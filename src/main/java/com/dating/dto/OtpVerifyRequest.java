package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phoneNumber;
    @NotBlank @Pattern(regexp = "^\\d{6}$")
    private String otp;
}
