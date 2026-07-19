package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Discovery, likes, and matching APIs")
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/api/v1/discover/swipe")
    @Operation(summary = "Swipe left/right on a profile")
    public ResponseEntity<ApiResponse<SwipeResponse>> swipe(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SwipeRequest request) {
        return ResponseEntity.ok(matchService.swipe(userId, request));
    }

    @PostMapping("/api/v1/discover/undo")
    @Operation(summary = "Undo last swipe (premium feature)")
    public ResponseEntity<ApiResponse<Void>> undoSwipe(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(matchService.undoSwipe(userId));
    }

    @GetMapping("/api/v1/discover")
    @Operation(summary = "Discover profiles")
    public ResponseEntity<ApiResponse<List<DiscoverProfile>>> discover(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) FilterRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (filters == null) filters = new FilterRequest();
        return ResponseEntity.ok(matchService.discover(userId, filters, page, size));
    }

    @GetMapping("/api/v1/matches")
    @Operation(summary = "Get all matches")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMatches(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(matchService.getMatches(userId, page, size));
    }

    @GetMapping("/api/v1/likes")
    @Operation(summary = "Get profiles who liked you")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getLikes(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(matchService.getLikes(userId, page, size));
    }

    @GetMapping("/api/v1/matches/history")
    @Operation(summary = "Get matching history (inactive matches)")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(matchService.getHistory(userId, page, size));
    }

    @PostMapping("/api/v1/likes/{likeId}/accept")
    @Operation(summary = "Accept a like request")
    public ResponseEntity<ApiResponse<MatchResponse>> acceptLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String likeId) {
        return ResponseEntity.ok(matchService.acceptLike(userId, likeId));
    }

    @PostMapping("/api/v1/likes/{likeId}/reject")
    @Operation(summary = "Reject a like request")
    public ResponseEntity<ApiResponse<Void>> rejectLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String likeId) {
        return ResponseEntity.ok(matchService.rejectLike(userId, likeId));
    }

    @DeleteMapping("/api/v1/matches/{matchId}")
    @Operation(summary = "Unmatch a user")
    public ResponseEntity<ApiResponse<Void>> unmatch(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId) {
        return ResponseEntity.ok(matchService.unmatch(userId, matchId));
    }
}
