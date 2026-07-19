package com.dating.dto;

import lombok.Data;

@Data
public class PhotoResponse {
    private String id;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
    private Boolean isVerified;
}
