package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.entity.Match;
import com.dating.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RandomMatchService {

    private final StringRedisTemplate redisTemplate;
    private final MatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String QUEUE_KEY = "random_match_queue";

    @Transactional
    public ApiResponse<Void> joinQueue(String userId) {
        // First, check if someone else is waiting
        String partnerId = redisTemplate.opsForSet().pop(QUEUE_KEY);

        if (partnerId != null) {
            if (partnerId.equals(userId)) {
                // It was ourselves! Put it back and wait.
                redisTemplate.opsForSet().add(QUEUE_KEY, userId);
                return ApiResponse.success(null, "Joined queue, waiting for match...");
            }

            // We found a partner! Create a Match
            Match match = Match.builder()
                    .user1Id(userId)
                    .user2Id(partnerId)
                    .matchedAt(Instant.now())
                    .isActive(true)
                    .build();
            match = matchRepository.save(match);

            // Notify both users via WebSocket
            Map<String, Object> payload1 = Map.of(
                    "type", "MATCHED",
                    "matchId", match.getId(),
                    "partnerId", partnerId
            );
            Map<String, Object> payload2 = Map.of(
                    "type", "MATCHED",
                    "matchId", match.getId(),
                    "partnerId", userId
            );

            messagingTemplate.convertAndSendToUser(userId, "/queue/random-match", payload1);
            messagingTemplate.convertAndSendToUser(partnerId, "/queue/random-match", payload2);

            log.info("Matched user {} with partner {}", userId, partnerId);
            return ApiResponse.success(null, "Matched instantly!");
        }

        // No one is waiting, so we join the queue
        redisTemplate.opsForSet().add(QUEUE_KEY, userId);
        return ApiResponse.success(null, "Joined queue, waiting for match...");
    }

    public ApiResponse<Void> leaveQueue(String userId) {
        redisTemplate.opsForSet().remove(QUEUE_KEY, userId);
        return ApiResponse.success(null, "Left queue");
    }

    @Transactional
    public ApiResponse<Void> next(String userId, String currentMatchId) {
        // If they have a current match, unmatch or set it inactive
        if (currentMatchId != null) {
            matchRepository.unmatch(currentMatchId, Instant.now());
            // Notify partner that the match ended
            matchRepository.findById(currentMatchId).ifPresent(match -> {
                String partnerId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
                messagingTemplate.convertAndSendToUser(partnerId, "/queue/random-match", Map.of(
                        "type", "PARTNER_LEFT",
                        "matchId", currentMatchId
                ));
            });
        }
        
        // Join queue again to find a new person
        return joinQueue(userId);
    }
}
