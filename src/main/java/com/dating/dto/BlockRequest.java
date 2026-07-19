package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlockRequest {
    @NotBlank private String blockedUserId;
    private String reason;
}
