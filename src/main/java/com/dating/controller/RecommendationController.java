package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "AI-powered profile recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Get personalized profile recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) RecommendationRequest request) {
        if (request == null) request = new RecommendationRequest();
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, request));
    }

    @GetMapping("/boosted")
    @Operation(summary = "Get boosted/premium profiles")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getBoostedProfiles(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(recommendationService.getBoostedProfiles(userId));
    }

    @GetMapping("/compatibility/{targetUserId}")
    @Operation(summary = "Get compatibility score with a user")
    public ResponseEntity<ApiResponse<Double>> getCompatibility(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String targetUserId) {
        return ResponseEntity.ok(recommendationService.getCompatibilityScore(userId, targetUserId));
    }
}
