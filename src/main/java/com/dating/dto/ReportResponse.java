package com.dating.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ReportResponse {
    private String id;
    private String reporterId;
    private String reportedUserId;
    private String reason;
    private String description;
    private String status;
    private String reviewedBy;
    private String resolutionNotes;
    private Instant createdAt;
}
