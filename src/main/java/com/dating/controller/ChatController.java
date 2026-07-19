package com.dating.controller;

import com.dating.dto.*;
import com.dating.service.ChatService;
import com.dating.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Messaging and chat APIs")
public class ChatController {

    private final ChatService chatService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/v1/messages")
    @Operation(summary = "Send a message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(userId, request));
    }

    @GetMapping("/api/v1/messages/{matchId}")
    @Operation(summary = "Get messages for a match")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(userId, matchId, page, size));
    }

    @PostMapping("/api/v1/messages/{matchId}/read")
    @Operation(summary = "Mark messages as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String matchId) {
        return ResponseEntity.ok(chatService.markAsRead(userId, matchId));
    }

    @GetMapping("/api/v1/chats")
    @Operation(summary = "Get all chat rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getChatRooms(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(chatService.getChatRooms(userId));
    }

    @DeleteMapping("/api/v1/messages/{messageId}")
    @Operation(summary = "Delete a message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String messageId) {
        return ResponseEntity.ok(chatService.deleteMessage(userId, messageId));
    }

    // WebSocket endpoints
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public WebSocketMessage handleWebSocketMessage(@Payload WebSocketMessage message) {
        return message;
    }

    @MessageMapping("/call.signaling")
    public void handleCallSignaling(@Payload WebSocketMessage message) {
        if (message.getRecipientId() != null) {
            messagingTemplate.convertAndSendToUser(
                message.getRecipientId(),
                "/queue/call",
                message
            );
        }
    }
}
