package com.dating.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhotoUploadRequest {
    @NotBlank private String imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
}
