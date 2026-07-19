package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.exception.DatingAppException;
import com.dating.dto.*;
import com.dating.entity.*;
import com.dating.mapper.ProfileMapper;
import com.dating.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository photoRepository;
    private final InterestRepository interestRepository;
    private final ProfileInterestRepository profileInterestRepository;
    private final ProfileMapper profileMapper;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ApiResponse<ProfileResponse> createOrUpdateProfile(String userId, ProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = profileMapper.toEntity(request);
            profile.setUserId(userId);
        } else {
            profile.setName(request.getName());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setGender(request.getGender());
            profile.setInterestedIn(request.getInterestedIn());
            profile.setBio(request.getBio());
            profile.setOccupation(request.getOccupation());
            profile.setEducation(request.getEducation());
            profile.setHeightCm(request.getHeightCm());
            profile.setReligion(request.getReligion());
            profile.setLanguages(request.getLanguages());
            profile.setLatitude(request.getLatitude());
            profile.setLongitude(request.getLongitude());
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
        }
        profile.setProfileCompletion(calculateCompletion(profile));
        profile = profileRepository.save(profile);
        redisTemplate.delete("profile:" + userId);
        return ApiResponse.success(buildProfileResponse(profile), "Profile saved successfully");
    }

    @Transactional(readOnly = true)
    public ApiResponse<ProfileResponse> getProfile(String userId) {
        String cacheKey = "profile:" + userId;
        // Check cache (simplified - in production use proper cache manager)
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        return ApiResponse.success(buildProfileResponse(profile));
    }

    @Transactional(readOnly = true)
    public ApiResponse<ProfileResponse> getProfileByUserId(String targetUserId, String currentUserId) {
        Profile profile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        if (!profile.getIsVisible() && !targetUserId.equals(currentUserId)) {
            throw DatingAppException.forbidden("Profile is hidden");
        }
        return ApiResponse.success(buildProfileResponse(profile));
    }

    @Transactional
    public ApiResponse<PhotoResponse> addPhoto(String userId, PhotoUploadRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        long photoCount = photoRepository.findByProfileIdOrderByDisplayOrderAsc(profile.getId()).size();
        if (photoCount >= 6) throw DatingAppException.badRequest("Maximum 6 photos allowed");

        ProfilePhoto photo = ProfilePhoto.builder()
                .profile(profile)
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : (int) photoCount + 1)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : photoCount == 0)
                .build();
        photo = photoRepository.save(photo);
        redisTemplate.delete("profile:" + userId);

        PhotoResponse response = new PhotoResponse();
        response.setId(photo.getId());
        response.setImageUrl(photo.getImageUrl());
        response.setDisplayOrder(photo.getDisplayOrder());
        response.setIsPrimary(photo.getIsPrimary());
        response.setIsVerified(photo.getIsVerified());
        return ApiResponse.success(response, "Photo added successfully");
    }

    @Transactional
    public ApiResponse<Void> deletePhoto(String userId, String photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> DatingAppException.notFound("Photo"));
        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw DatingAppException.forbidden("Cannot delete this photo");
        }
        photoRepository.delete(photo);
        redisTemplate.delete("profile:" + userId);
        return ApiResponse.success(null, "Photo deleted successfully");
    }

    @Transactional
    public ApiResponse<Void> addInterests(String userId, List<String> interestNames) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        profileInterestRepository.deleteByProfileId(profile.getId());
        for (String name : interestNames) {
            Interest interest = interestRepository.findByName(name)
                    .orElseGet(() -> interestRepository.save(Interest.builder().name(name).build()));
            profileInterestRepository.save(ProfileInterest.builder()
                    .profile(profile).interest(interest).build());
        }
        redisTemplate.delete("profile:" + userId);
        return ApiResponse.success(null, "Interests updated");
    }

    @Transactional
    public ApiResponse<Void> updatePrivacy(String userId, PrivacySettings settings) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> DatingAppException.notFound("Profile"));
        if (settings.getIsVisible() != null) profile.setIsVisible(settings.getIsVisible());
        if (settings.getHideAge() != null) profile.setHideAge(settings.getHideAge());
        if (settings.getHideDistance() != null) profile.setHideDistance(settings.getHideDistance());
        profileRepository.save(profile);
        redisTemplate.delete("profile:" + userId);
        return ApiResponse.success(null, "Privacy settings updated");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Interest>> getAllInterests() {
        return ApiResponse.success(interestRepository.findAll());
    }

    private ProfileResponse buildProfileResponse(Profile profile) {
        ProfileResponse response = profileMapper.toResponse(profile);
        List<PhotoResponse> photos = photoRepository.findByProfileIdOrderByDisplayOrderAsc(profile.getId())
                .stream().map(p -> {
                    PhotoResponse pr = new PhotoResponse();
                    pr.setId(p.getId());
                    pr.setImageUrl(p.getImageUrl());
                    pr.setDisplayOrder(p.getDisplayOrder());
                    pr.setIsPrimary(p.getIsPrimary());
                    pr.setIsVerified(p.getIsVerified());
                    return pr;
                }).collect(Collectors.toList());
        response.setPhotos(photos);
        List<String> interests = profileInterestRepository.findByProfileId(profile.getId())
                .stream().map(pi -> pi.getInterest().getName()).collect(Collectors.toList());
        response.setInterests(interests);
        return response;
    }

    private int calculateCompletion(Profile profile) {
        int score = 0;
        if (profile.getName() != null && !profile.getName().isEmpty()) score += 15;
        if (profile.getBio() != null && !profile.getBio().isEmpty()) score += 15;
        if (profile.getOccupation() != null) score += 10;
        if (profile.getEducation() != null) score += 10;
        if (profile.getHeightCm() != null) score += 5;
        if (profile.getReligion() != null) score += 5;
        if (profile.getLanguages() != null && !profile.getLanguages().isEmpty()) score += 10;
        if (profile.getLatitude() != null && profile.getLongitude() != null) score += 10;
        if (profile.getCity() != null) score += 10;
        if (profile.getCountry() != null) score += 5;
        if (profile.getPhotos() != null && !profile.getPhotos().isEmpty()) score += 5;
        return Math.min(score, 100);
    }
}
