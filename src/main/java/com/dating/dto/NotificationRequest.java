package com.dating.dto;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {
    private String userId;
    private String title;
    private String body;
    private String type;
    private Map<String, String> data;
}
