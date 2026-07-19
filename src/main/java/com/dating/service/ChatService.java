package com.dating.service;

import com.dating.dto.*;
import com.dating.entity.ChatRoom;
import com.dating.entity.Message;
import com.dating.repository.ChatRoomRepository;
import com.dating.repository.MessageRepository;
import com.dating.dto.ApiResponse;
import com.dating.exception.DatingAppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ApiResponse<MessageResponse> sendMessage(String senderId, MessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findByMatchId(request.getMatchId())
                .orElseThrow(() -> DatingAppException.notFound("Chat room"));

        if (!chatRoom.getUser1Id().equals(senderId) && !chatRoom.getUser2Id().equals(senderId)) {
            throw DatingAppException.forbidden("Not a participant in this chat");
        }
        if (!chatRoom.getIsActive()) {
            throw DatingAppException.badRequest("Chat is no longer active");
        }

        Message message = Message.builder()
                .matchId(request.getMatchId())
                .senderId(senderId)
                .content(request.getContent())
                .messageType(Message.MessageType.valueOf(request.getMessageType().toUpperCase()))
                .mediaUrl(request.getMediaUrl())
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        // Update chat room
        chatRoomRepository.updateLastMessage(request.getMatchId(), request.getContent(), Instant.now());
        chatRoomRepository.incrementUser1Unread(request.getMatchId(), senderId);
        chatRoomRepository.incrementUser2Unread(request.getMatchId(), senderId);

        // Send via WebSocket
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType("MESSAGE");
        wsMessage.setMatchId(request.getMatchId());
        wsMessage.setSenderId(senderId);
        wsMessage.setContent(request.getContent());
        wsMessage.setMessageType(request.getMessageType());
        wsMessage.setMediaUrl(request.getMediaUrl());
        wsMessage.setTimestamp(Instant.now().toEpochMilli());

        messagingTemplate.convertAndSend("/topic/match/" + request.getMatchId(), wsMessage);

        // Notify via Kafka for push notifications
        String receiverId = chatRoom.getUser1Id().equals(senderId) ? chatRoom.getUser2Id() : chatRoom.getUser1Id();
        kafkaTemplate.send("notification-events", Map.of(
                "event", "NEW_MESSAGE",
                "matchId", request.getMatchId(),
                "senderId", senderId,
                "receiverId", receiverId,
                "content", request.getContent()
        ));

        return ApiResponse.success(toMessageResponse(message), "Message sent");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<MessageResponse>> getMessages(String userId, String matchId, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findByMatchId(matchId)
                .orElseThrow(() -> DatingAppException.notFound("Chat room"));
        if (!chatRoom.getUser1Id().equals(userId) && !chatRoom.getUser2Id().equals(userId)) {
            throw DatingAppException.forbidden("Not a participant in this chat");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByMatchIdAndIsDeletedFalseOrderByCreatedAtDesc(matchId, pageable);

        List<MessageResponse> responses = messages.getContent().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @Transactional
    public ApiResponse<Void> markAsRead(String userId, String matchId) {
        ChatRoom chatRoom = chatRoomRepository.findByMatchId(matchId)
                .orElseThrow(() -> DatingAppException.notFound("Chat room"));
        if (!chatRoom.getUser1Id().equals(userId) && !chatRoom.getUser2Id().equals(userId)) {
            throw DatingAppException.forbidden("Not a participant in this chat");
        }

        messageRepository.markMessagesAsRead(matchId, userId, Instant.now());
        chatRoomRepository.resetUser1Unread(matchId, userId);
        chatRoomRepository.resetUser2Unread(matchId, userId);

        // Send read receipt via WebSocket
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType("READ_RECEIPT");
        wsMessage.setMatchId(matchId);
        wsMessage.setSenderId(userId);
        wsMessage.setTimestamp(Instant.now().toEpochMilli());
        messagingTemplate.convertAndSend("/topic/match/" + matchId, wsMessage);

        return ApiResponse.success(null, "Messages marked as read");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ChatRoomResponse>> getChatRooms(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findActiveChatRoomsByUserId(userId);
        List<ChatRoomResponse> responses = chatRooms.stream().map(c -> {
            ChatRoomResponse cr = new ChatRoomResponse();
            cr.setMatchId(c.getMatchId());
            String otherUserId = c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id();
            cr.setOtherUserId(otherUserId);
            cr.setLastMessage(c.getLastMessage());
            cr.setLastMessageAt(c.getLastMessageAt());
            cr.setIsActive(c.getIsActive());
            cr.setUnreadCount(c.getUser1Id().equals(userId) ? c.getUser1Unread() : c.getUser2Unread());
            return cr;
        }).collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @Transactional
    public ApiResponse<Void> deleteMessage(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> DatingAppException.notFound("Message"));
        if (!message.getSenderId().equals(userId)) {
            throw DatingAppException.forbidden("Cannot delete this message");
        }
        message.setIsDeleted(true);
        messageRepository.save(message);
        return ApiResponse.success(null, "Message deleted");
    }

    @Transactional
    public void handleTyping(String userId, String matchId, boolean isTyping) {
        ChatRoom chatRoom = chatRoomRepository.findByMatchId(matchId).orElse(null);
        if (chatRoom == null) return;

        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType("TYPING");
        wsMessage.setMatchId(matchId);
        wsMessage.setSenderId(userId);
        wsMessage.setIsTyping(isTyping);
        wsMessage.setTimestamp(Instant.now().toEpochMilli());
        messagingTemplate.convertAndSend("/topic/match/" + matchId, wsMessage);
    }

    @Transactional
    public ApiResponse<Void> createChatRoom(String matchId, String user1Id, String user2Id) {
        if (chatRoomRepository.findByMatchId(matchId).isPresent()) {
            return ApiResponse.success(null, "Chat room already exists");
        }
        ChatRoom room = ChatRoom.builder()
                .matchId(matchId)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .isActive(true)
                .build();
        chatRoomRepository.save(room);
        return ApiResponse.success(null, "Chat room created");
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse mr = new MessageResponse();
        mr.setId(message.getId());
        mr.setMatchId(message.getMatchId());
        mr.setSenderId(message.getSenderId());
        mr.setContent(message.getIsDeleted() ? "This message was deleted" : message.getContent());
        mr.setMessageType(message.getMessageType().name());
        mr.setMediaUrl(message.getMediaUrl());
        mr.setIsRead(message.getIsRead());
        mr.setReadAt(message.getReadAt());
        mr.setCreatedAt(message.getCreatedAt());
        return mr;
    }
}
