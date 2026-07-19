package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.service.RandomMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/random")
@RequiredArgsConstructor
public class RandomMatchController {

    private final RandomMatchService randomMatchService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> joinQueue(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(randomMatchService.joinQueue(userId));
    }

    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveQueue(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(randomMatchService.leaveQueue(userId));
    }

    @PostMapping("/next")
    public ResponseEntity<ApiResponse<Void>> next(@RequestHeader("X-User-Id") String userId, @RequestParam(required = false) String currentMatchId) {
        return ResponseEntity.ok(randomMatchService.next(userId, currentMatchId));
    }
}
