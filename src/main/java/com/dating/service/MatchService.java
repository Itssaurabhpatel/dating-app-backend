package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.exception.DatingAppException;
import com.dating.service.ProfileService;
import com.dating.dto.*;
import com.dating.entity.Like;
import com.dating.entity.Match;
import com.dating.entity.Swipe;
import com.dating.repository.LikeRepository;
import com.dating.repository.MatchRepository;
import com.dating.repository.SwipeRepository;
import com.dating.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final LikeRepository likeRepository;
    private final MatchRepository matchRepository;
    private final SwipeRepository swipeRepository;
    private final ProfileService profileService;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final int DAILY_SWIPE_LIMIT = 50;
    private static final int SUPER_LIKE_LIMIT = 1;

    @Transactional
    public ApiResponse<SwipeResponse> swipe(String userId, SwipeRequest request) {
        if (userId.equals(request.getTargetId())) {
            throw DatingAppException.badRequest("Cannot swipe on yourself");
        }

        // Check daily swipe limit for non-premium users
        String swipeKey = "swipes:" + userId + ":" + Instant.now().truncatedTo(ChronoUnit.DAYS);
        String swipeCount = redisTemplate.opsForValue().get(swipeKey);
        if (swipeCount != null && Integer.parseInt(swipeCount) >= DAILY_SWIPE_LIMIT) {
            throw DatingAppException.badRequest("Daily swipe limit reached. Upgrade to premium for unlimited swipes.");
        }
        redisTemplate.opsForValue().increment(swipeKey);
        redisTemplate.expire(swipeKey, 24, java.util.concurrent.TimeUnit.HOURS);

        boolean isSuperLike = Boolean.TRUE.equals(request.getIsSuperLike());
        if (isSuperLike) {
            String superLikeKey = "superlikes:" + userId + ":" + Instant.now().truncatedTo(ChronoUnit.DAYS);
            String superLikeCount = redisTemplate.opsForValue().get(superLikeKey);
            if (superLikeCount != null && Integer.parseInt(superLikeCount) >= SUPER_LIKE_LIMIT) {
                throw DatingAppException.badRequest("Daily super like limit reached");
            }
            redisTemplate.opsForValue().increment(superLikeKey);
            redisTemplate.expire(superLikeKey, 24, java.util.concurrent.TimeUnit.HOURS);
        }

        // Save swipe record
        Swipe swipe = Swipe.builder()
                .userId(userId)
                .targetId(request.getTargetId())
                .direction(Swipe.SwipeDirection.valueOf(request.getDirection().toUpperCase()))
                .isSuperLike(isSuperLike)
                .build();
        swipeRepository.save(swipe);

        if ("LEFT".equalsIgnoreCase(request.getDirection())) {
            return ApiResponse.success(SwipeResponse.builder()
                    .isMatch(false)
                    .message("Skipped").build());
        }

        // Check for mutual like
        Optional<Like> existingLike = likeRepository.findBySenderIdAndReceiverId(request.getTargetId(), userId);
        if (existingLike.isPresent() && existingLike.get().getStatus() == Like.LikeStatus.PENDING) {
            // It's a match!
            Like like = existingLike.get();
            like.setStatus(Like.LikeStatus.MATCHED);
            likeRepository.save(like);

            Match match = Match.builder()
                    .user1Id(userId)
                    .user2Id(request.getTargetId())
                    .matchedAt(Instant.now())
                    .isActive(true)
                    .build();
            match = matchRepository.save(match);

            kafkaTemplate.send("match-events", Map.of(
                    "event", "MATCH_CREATED",
                    "matchId", match.getId(),
                    "user1Id", userId,
                    "user2Id", request.getTargetId(),
                    "isSuperLike", isSuperLike
            ));

            return ApiResponse.success(SwipeResponse.builder()
                    .isMatch(true)
                    .matchId(match.getId())
                    .message("It's a match!").build());
        }

        // Create pending like
        Like newLike = Like.builder()
                .senderId(userId)
                .receiverId(request.getTargetId())
                .type(Like.LikeType.LIKE)
                .status(Like.LikeStatus.PENDING)
                .isSuperLike(isSuperLike)
                .build();
        likeRepository.save(newLike);

        kafkaTemplate.send("like-events", Map.of(
                "event", "PROFILE_LIKED",
                "senderId", userId,
                "receiverId", request.getTargetId(),
                "isSuperLike", isSuperLike
        ));

        return ApiResponse.success(SwipeResponse.builder()
                .isMatch(false)
                .message(isSuperLike ? "Super Like sent!" : "Like sent!").build());
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<MatchResponse>> getMatches(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Match> matches = matchRepository.findActiveMatchesByUserId(userId, pageable);

        List<MatchResponse> responses = matches.getContent().stream().map(m -> {
            String otherUserId = m.getUser1Id().equals(userId) ? m.getUser2Id() : m.getUser1Id();
            MatchResponse mr = new MatchResponse();
            mr.setId(m.getId());
            mr.setUserId(otherUserId);
            mr.setMatchedAt(m.getMatchedAt());
            mr.setLastMessageAt(m.getLastMessageAt());
            // Fetch profile info (with fallback)
            try {
                ApiResponse<ProfileResponse> profile = profileService.getProfile(otherUserId);
                if (profile.isSuccess() && profile.getData() != null) {
                    mr.setName(profile.getData().getName());
                    if (profile.getData().getPhotos() != null && !profile.getData().getPhotos().isEmpty()) {
                        mr.setProfilePhotoUrl(profile.getData().getPhotos().get(0).getImageUrl());
                    }
                }
            } catch (Exception e) {
                mr.setName("User");
            }
            return mr;
        }).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<MatchResponse>> getLikes(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Like> likes = likeRepository.findByReceiverIdAndStatus(userId, Like.LikeStatus.PENDING, pageable);

        List<MatchResponse> responses = likes.getContent().stream().map(l -> {
            MatchResponse mr = new MatchResponse();
            // In a Like, the ID of the MatchResponse should be the Like ID so we can accept/reject it.
            // Wait, MatchResponse doesn't have a likeId field. Let's use the `id` field for the Like ID.
            mr.setId(l.getId()); 
            mr.setUserId(l.getSenderId());
            mr.setMatchedAt(l.getCreatedAt());
            try {
                ApiResponse<ProfileResponse> profile = profileService.getProfile(l.getSenderId());
                if (profile.isSuccess() && profile.getData() != null) {
                    mr.setName(profile.getData().getName());
                    if (profile.getData().getPhotos() != null && !profile.getData().getPhotos().isEmpty()) {
                        mr.setProfilePhotoUrl(profile.getData().getPhotos().get(0).getImageUrl());
                    }
                }
            } catch (Exception e) {
                mr.setName("User");
            }
            return mr;
        }).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @Transactional
    public ApiResponse<MatchResponse> acceptLike(String userId, String likeId) {
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> DatingAppException.notFound("Like request not found"));
        
        if (!like.getReceiverId().equals(userId)) {
            throw DatingAppException.forbidden("Not authorized to accept this like");
        }
        
        like.setStatus(Like.LikeStatus.MATCHED);
        likeRepository.save(like);

        Match match = Match.builder()
                .user1Id(like.getSenderId())
                .user2Id(userId)
                .matchedAt(Instant.now())
                .isActive(true)
                .build();
        match = matchRepository.save(match);
        
        return ApiResponse.success(null, "Match accepted!");
    }

    @Transactional
    public ApiResponse<Void> rejectLike(String userId, String likeId) {
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> DatingAppException.notFound("Like request not found"));
        
        if (!like.getReceiverId().equals(userId)) {
            throw DatingAppException.forbidden("Not authorized to reject this like");
        }
        
        like.setStatus(Like.LikeStatus.DECLINED);
        likeRepository.save(like);
        
        return ApiResponse.success(null, "Match rejected");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<MatchResponse>> getHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Match> matches = matchRepository.findHistoryByUserId(userId, pageable);

        List<MatchResponse> responses = matches.getContent().stream().map(m -> {
            String otherUserId = m.getUser1Id().equals(userId) ? m.getUser2Id() : m.getUser1Id();
            MatchResponse mr = new MatchResponse();
            mr.setId(m.getId());
            mr.setUserId(otherUserId);
            mr.setMatchedAt(m.getMatchedAt());
            
            // For history, lastMessageAt can represent the unmatched time temporarily to pass it to frontend
            mr.setLastMessageAt(m.getUnmatchedAt()); 
            
            try {
                ApiResponse<ProfileResponse> profile = profileService.getProfile(otherUserId);
                if (profile.isSuccess() && profile.getData() != null) {
                    mr.setName(profile.getData().getName());
                    if (profile.getData().getPhotos() != null && !profile.getData().getPhotos().isEmpty()) {
                        mr.setProfilePhotoUrl(profile.getData().getPhotos().get(0).getImageUrl());
                    }
                }
            } catch (Exception e) {
                mr.setName("Unknown User");
            }
            return mr;
        }).collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @Transactional
    public ApiResponse<Void> unmatch(String userId, String matchId) {
        Match match = matchRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> DatingAppException.notFound("Match"));
        matchRepository.unmatch(matchId, Instant.now());
        return ApiResponse.success(null, "Unmatched successfully");
    }

    @Transactional
    public ApiResponse<Void> undoSwipe(String userId) {
        Optional<Swipe> lastSwipe = swipeRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId))
                .max(Comparator.comparing(Swipe::getCreatedAt));

        if (lastSwipe.isPresent()) {
            Swipe swipe = lastSwipe.get();
            if (swipe.getCreatedAt().isAfter(Instant.now().minusSeconds(60))) {
                swipeRepository.delete(swipe);
                if (swipe.getDirection() == Swipe.SwipeDirection.RIGHT) {
                    likeRepository.findBySenderIdAndReceiverId(userId, swipe.getTargetId())
                            .ifPresent(likeRepository::delete);
                }
                redisTemplate.opsForValue().decrement("swipes:" + userId + ":" + Instant.now().truncatedTo(ChronoUnit.DAYS));
                return ApiResponse.success(null, "Last swipe undone");
            }
        }
        throw DatingAppException.badRequest("Cannot undo swipe after 60 seconds");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<DiscoverProfile>> discover(String userId, FilterRequest filters, int page, int size) {
        // Get user's profile for preferences and location
        ApiResponse<ProfileResponse> myProfileResp = profileService.getProfile(userId);
        if (!myProfileResp.isSuccess() || myProfileResp.getData() == null) {
            throw DatingAppException.notFound("Profile not found");
        }
        ProfileResponse myProfile = myProfileResp.getData();

        // Get already swiped/liked users to exclude
        Set<String> excludedIds = new HashSet<>();
        excludedIds.add(userId);
        likeRepository.findAllByUserId(userId).forEach(l -> {
            excludedIds.add(l.getSenderId());
            excludedIds.add(l.getReceiverId());
        });
        swipeRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId))
                .forEach(s -> excludedIds.add(s.getTargetId()));

        // Calculate bounding box for distance filter
        double radiusKm = filters.getMaxDistance() != null ? filters.getMaxDistance() : 50;
        double[] bbox = com.dating.util.GeoUtils.calculateBoundingBox(
                myProfile.getLatitude(), myProfile.getLongitude(), radiusKm);

        // In production, query Elasticsearch or use spatial queries
        // For now, return mock data structure
        return ApiResponse.success(Collections.emptyList());
    }
}
