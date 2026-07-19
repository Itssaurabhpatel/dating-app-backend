package com.dating.dto;

import com.dating.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank private String reportedUserId;
    @NotNull private Report.ReportReason reason;
    private String description;
    private String matchId;
    private String messageId;
}
