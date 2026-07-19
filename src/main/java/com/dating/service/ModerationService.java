package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.exception.DatingAppException;
import com.dating.dto.*;
import com.dating.entity.*;
import com.dating.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ReportRepository reportRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ApiResponse<ReportResponse> createReport(String reporterId, ReportRequest request) {
        if (reporterId.equals(request.getReportedUserId())) {
            throw DatingAppException.badRequest("Cannot report yourself");
        }
        Report report = Report.builder()
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .reason(request.getReason())
                .description(request.getDescription())
                .matchId(request.getMatchId())
                .messageId(request.getMessageId())
                .status(Report.ReportStatus.PENDING)
                .build();
        report = reportRepository.save(report);

        // Auto-block if threshold reached
        long reportCount = reportRepository.countByReportedUserId(request.getReportedUserId());
        if (reportCount >= 5) {
            kafkaTemplate.send("user-events", Map.of(
                    "event", "USER_SUSPENDED",
                    "userId", request.getReportedUserId(),
                    "reason", "Multiple reports"
            ));
        }

        return ApiResponse.success(toReportResponse(report), "Report submitted");
    }

    @Transactional
    public ApiResponse<Void> blockUser(String userId, BlockRequest request) {
        if (userId.equals(request.getBlockedUserId())) {
            throw DatingAppException.badRequest("Cannot block yourself");
        }
        if (blockedUserRepository.existsByUserIdAndBlockedUserId(userId, request.getBlockedUserId())) {
            throw DatingAppException.conflict("User already blocked");
        }
        BlockedUser blocked = BlockedUser.builder()
                .userId(userId)
                .blockedUserId(request.getBlockedUserId())
                .reason(request.getReason())
                .build();
        blockedUserRepository.save(blocked);
        return ApiResponse.success(null, "User blocked");
    }

    @Transactional
    public ApiResponse<Void> unblockUser(String userId, String blockedUserId) {
        BlockedUser blocked = blockedUserRepository.findByUserIdAndBlockedUserId(userId, blockedUserId)
                .orElseThrow(() -> DatingAppException.notFound("Block record"));
        blockedUserRepository.delete(blocked);
        return ApiResponse.success(null, "User unblocked");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ReportResponse>> getMyReports(String userId) {
        List<ReportResponse> reports = reportRepository.findByReporterId(userId).stream()
                .map(this::toReportResponse).collect(Collectors.toList());
        return ApiResponse.success(reports);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BlockedUser>> getBlockedUsers(String userId) {
        return ApiResponse.success(blockedUserRepository.findByUserId(userId));
    }

    // Admin endpoints
    @Transactional(readOnly = true)
    public ApiResponse<Page<ReportResponse>> getPendingReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = reportRepository.findByStatus(Report.ReportStatus.PENDING, pageable);
        return ApiResponse.success(reports.map(this::toReportResponse));
    }

    @Transactional
    public ApiResponse<Void> resolveReport(String moderatorId, String reportId, ResolveReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> DatingAppException.notFound("Report"));
        reportRepository.resolveReport(reportId, request.getStatus(), moderatorId, request.getResolutionNotes());

        ModerationLog log = ModerationLog.builder()
                .moderatorId(moderatorId)
                .action("RESOLVE_REPORT")
                .targetReportId(reportId)
                .targetUserId(report.getReportedUserId())
                .details("Status: " + request.getStatus() + ", Notes: " + request.getResolutionNotes())
                .build();
        moderationLogRepository.save(log);

        if (request.getStatus() == Report.ReportStatus.RESOLVED) {
            kafkaTemplate.send("user-events", Map.of(
                    "event", "USER_SUSPENDED",
                    "userId", report.getReportedUserId(),
                    "reason", report.getReason().name()
            ));
        }

        return ApiResponse.success(null, "Report resolved");
    }

    private ReportResponse toReportResponse(Report report) {
        ReportResponse r = new ReportResponse();
        r.setId(report.getId());
        r.setReporterId(report.getReporterId());
        r.setReportedUserId(report.getReportedUserId());
        r.setReason(report.getReason().name());
        r.setDescription(report.getDescription());
        r.setStatus(report.getStatus().name());
        r.setReviewedBy(report.getReviewedBy());
        r.setResolutionNotes(report.getResolutionNotes());
        r.setCreatedAt(report.getCreatedAt());
        return r;
    }
}
