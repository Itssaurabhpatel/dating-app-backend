package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.entity.BlockedUser;
import com.dating.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moderation", description = "Reports, blocks, and moderation APIs")
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/api/v1/reports")
    @Operation(summary = "Report a user")
    public ResponseEntity<ApiResponse<ReportResponse>> reportUser(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(moderationService.createReport(userId, request));
    }

    @GetMapping("/api/v1/reports/my")
    @Operation(summary = "Get my reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(moderationService.getMyReports(userId));
    }

    @PostMapping("/api/v1/blocks")
    @Operation(summary = "Block a user")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody BlockRequest request) {
        return ResponseEntity.ok(moderationService.blockUser(userId, request));
    }

    @DeleteMapping("/api/v1/blocks/{blockedUserId}")
    @Operation(summary = "Unblock a user")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String blockedUserId) {
        return ResponseEntity.ok(moderationService.unblockUser(userId, blockedUserId));
    }

    @GetMapping("/api/v1/blocks")
    @Operation(summary = "Get blocked users")
    public ResponseEntity<ApiResponse<List<BlockedUser>>> getBlockedUsers(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(moderationService.getBlockedUsers(userId));
    }

    // Admin endpoints
    @GetMapping("/api/v1/moderation/reports/pending")
    @Operation(summary = "Get pending reports (Admin)")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(moderationService.getPendingReports(page, size));
    }

    @PostMapping("/api/v1/moderation/reports/{reportId}/resolve")
    @Operation(summary = "Resolve a report (Admin)")
    public ResponseEntity<ApiResponse<Void>> resolveReport(
            @RequestHeader("X-User-Id") String moderatorId,
            @PathVariable String reportId,
            @Valid @RequestBody ResolveReportRequest request) {
        return ResponseEntity.ok(moderationService.resolveReport(moderatorId, reportId, request));
    }
}
