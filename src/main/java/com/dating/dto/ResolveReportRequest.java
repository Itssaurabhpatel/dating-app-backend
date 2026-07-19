package com.dating.dto;

import com.dating.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {
    @NotNull private Report.ReportStatus status;
    @NotBlank private String resolutionNotes;
}
