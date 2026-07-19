package com.dating.controller;

import com.dating.dto.ApiResponse;
import com.dating.dto.LiveRoomRequest;
import com.dating.dto.LiveRoomResponse;
import com.dating.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Live Rooms", description = "Live audio and video rooms")
public class LiveRoomController {

    private final LiveRoomService liveRoomService;

    @GetMapping
    @Operation(summary = "Get all active live rooms")
    public ResponseEntity<ApiResponse<List<LiveRoomResponse>>> getActiveRooms() {
        return ResponseEntity.ok(liveRoomService.getActiveRooms());
    }

    @PostMapping
    @Operation(summary = "Create a new live room")
    public ResponseEntity<ApiResponse<LiveRoomResponse>> createRoom(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody LiveRoomRequest request) {
        return ResponseEntity.ok(liveRoomService.createRoom(userId, request));
    }

    @PostMapping("/{roomId}/join")
    @Operation(summary = "Join a live room")
    public ResponseEntity<ApiResponse<LiveRoomResponse>> joinRoom(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String roomId) {
        return ResponseEntity.ok(liveRoomService.joinRoom(userId, roomId));
    }

    @PostMapping("/{roomId}/leave")
    @Operation(summary = "Leave a live room")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String roomId) {
        return ResponseEntity.ok(liveRoomService.leaveRoom(userId, roomId));
    }
}
