package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.*;
import com.dating.entity.Interest;
import com.dating.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "User profile management APIs")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @Operation(summary = "Create or update profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> createOrUpdateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.createOrUpdateProfile(userId, request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get profile by user ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId, currentUserId));
    }

    @PostMapping("/photos")
    @Operation(summary = "Add profile photo")
    public ResponseEntity<ApiResponse<PhotoResponse>> addPhoto(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PhotoUploadRequest request) {
        return ResponseEntity.ok(profileService.addPhoto(userId, request));
    }

    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "Delete profile photo")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String photoId) {
        return ResponseEntity.ok(profileService.deletePhoto(userId, photoId));
    }

    @PostMapping("/interests")
    @Operation(summary = "Update interests")
    public ResponseEntity<ApiResponse<Void>> updateInterests(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody List<String> interests) {
        return ResponseEntity.ok(profileService.addInterests(userId, interests));
    }

    @PutMapping("/privacy")
    @Operation(summary = "Update privacy settings")
    public ResponseEntity<ApiResponse<Void>> updatePrivacy(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PrivacySettings settings) {
        return ResponseEntity.ok(profileService.updatePrivacy(userId, settings));
    }

    @GetMapping("/interests/all")
    @Operation(summary = "Get all available interests")
    public ResponseEntity<ApiResponse<List<Interest>>> getAllInterests() {
        return ResponseEntity.ok(profileService.getAllInterests());
    }
}
