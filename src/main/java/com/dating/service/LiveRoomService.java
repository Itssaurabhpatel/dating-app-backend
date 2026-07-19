package com.dating.service;

import com.dating.dto.ApiResponse;
import com.dating.dto.LiveRoomRequest;
import com.dating.dto.LiveRoomResponse;
import com.dating.entity.LiveRoom;
import com.dating.exception.DatingAppException;
import com.dating.repository.LiveRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveRoomService {

    private final LiveRoomRepository liveRoomRepository;

    @Transactional(readOnly = true)
    public ApiResponse<List<LiveRoomResponse>> getActiveRooms() {
        List<LiveRoomResponse> rooms = liveRoomRepository.findAll().stream()
                .map(LiveRoomResponse::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.success(rooms);
    }

    @Transactional
    public ApiResponse<LiveRoomResponse> createRoom(String userId, LiveRoomRequest request) {
        LiveRoom room = LiveRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .hostId(userId)
                .type(request.getType() != null ? request.getType() : "video")
                .maxParticipants(request.getMaxParticipants() > 0 ? request.getMaxParticipants() : 10)
                .participants(1) // the host
                .bgImage(request.getBgImage())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : "")
                .build();
        
        room = liveRoomRepository.save(room);
        return ApiResponse.success(LiveRoomResponse.fromEntity(room), "Room created");
    }

    @Transactional
    public ApiResponse<LiveRoomResponse> joinRoom(String userId, String roomId) {
        LiveRoom room = liveRoomRepository.findById(roomId)
                .orElseThrow(() -> DatingAppException.notFound("Room not found"));
        
        if (room.getParticipants() >= room.getMaxParticipants()) {
            throw DatingAppException.badRequest("Room is full");
        }
        
        room.setParticipants(room.getParticipants() + 1);
        room = liveRoomRepository.save(room);
        
        return ApiResponse.success(LiveRoomResponse.fromEntity(room), "Joined room");
    }

    @Transactional
    public ApiResponse<Void> leaveRoom(String userId, String roomId) {
        LiveRoom room = liveRoomRepository.findById(roomId)
                .orElseThrow(() -> DatingAppException.notFound("Room not found"));
        
        if (room.getParticipants() > 1) {
            room.setParticipants(room.getParticipants() - 1);
            liveRoomRepository.save(room);
        } else {
            // Room is empty, delete it
            liveRoomRepository.delete(room);
        }
        
        return ApiResponse.success(null, "Left room");
    }
}
