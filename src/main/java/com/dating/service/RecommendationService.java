package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.util.GeoUtils;
import com.dating.dto.ProfileResponse;
import com.dating.service.ProfileService;
import com.dating.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProfileService profileService;
    private final StringRedisTemplate redisTemplate;

    private static final String RECOMMENDATION_CACHE_PREFIX = "recommendations:";
    private static final int CACHE_TTL_HOURS = 1;

    public ApiResponse<List<RecommendationResponse>> getRecommendations(String userId, RecommendationRequest request) {
        String cacheKey = RECOMMENDATION_CACHE_PREFIX + userId + ":" + request.hashCode();
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // In production, deserialize cached JSON
            log.info("Returning cached recommendations for user: {}", userId);
        }

        // Get user profile
        ApiResponse<ProfileResponse> myProfileResp = profileService.getProfile(userId);
        if (!myProfileResp.isSuccess() || myProfileResp.getData() == null) {
            return ApiResponse.error("Could not load profile", 500);
        }
        ProfileResponse myProfile = myProfileResp.getData();

        // In production, query Elasticsearch for candidates within geo bounds
        // For now, return empty list with structure
        List<RecommendationResponse> recommendations = new ArrayList<>();

        // Cache results
        // redisTemplate.opsForValue().set(cacheKey, serialize(recommendations), CACHE_TTL_HOURS, TimeUnit.HOURS);

        return ApiResponse.success(recommendations);
    }

    public ApiResponse<List<RecommendationResponse>> getBoostedProfiles(String userId) {
        // Return premium/boosted profiles for "See Who Liked You" or boosted discovery
        return ApiResponse.success(Collections.emptyList());
    }

    public ApiResponse<Double> getCompatibilityScore(String userId, String targetUserId) {
        // AI-based compatibility scoring
        // In production, use ML model or collaborative filtering
        double score = Math.random() * 100;
        return ApiResponse.success(Math.round(score * 100.0) / 100.0);
    }

    private double calculateCompatibility(ProfileResponse p1, ProfileResponse p2) {
        double score = 0;
        // Age preference match
        int ageDiff = Math.abs(p1.getAge() - p2.getAge());
        score += Math.max(0, 20 - ageDiff);
        // Interest overlap
        if (p1.getInterests() != null && p2.getInterests() != null) {
            Set<String> common = new HashSet<>(p1.getInterests());
            common.retainAll(p2.getInterests());
            score += common.size() * 10;
        }
        // Distance
        if (p1.getLatitude() != null && p1.getLongitude() != null &&
            p2.getLatitude() != null && p2.getLongitude() != null) {
            double dist = GeoUtils.calculateDistance(p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude());
            score += Math.max(0, 30 - dist);
        }
        // Profile completion
        score += (p2.getProfileCompletion() != null ? p2.getProfileCompletion() : 0) * 0.2;
        return Math.min(score, 100);
    }
}
