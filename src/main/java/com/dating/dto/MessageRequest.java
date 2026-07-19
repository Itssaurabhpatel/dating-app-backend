package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank private String matchId;
    @NotBlank private String content;
    @NotNull private String messageType; // TEXT, IMAGE, etc.
    private String mediaUrl;
}
