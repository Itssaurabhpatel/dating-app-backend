package com.dating.repository;

import com.dating.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByMatchId(String matchId);

    @Query("SELECT c FROM ChatRoom c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    List<ChatRoom> findActiveChatRoomsByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE ChatRoom c SET c.lastMessage = :message, c.lastMessageAt = :timestamp WHERE c.matchId = :matchId")
    void updateLastMessage(@Param("matchId") String matchId, @Param("message") String message, @Param("timestamp") Instant timestamp);

    @Modifying
    @Query("UPDATE ChatRoom c SET c.user1Unread = c.user1Unread + 1 WHERE c.matchId = :matchId AND c.user1Id != :senderId")
    void incrementUser1Unread(@Param("matchId") String matchId, @Param("senderId") String senderId);

    @Modifying
    @Query("UPDATE ChatRoom c SET c.user2Unread = c.user2Unread + 1 WHERE c.matchId = :matchId AND c.user2Id != :senderId")
    void incrementUser2Unread(@Param("matchId") String matchId, @Param("senderId") String senderId);

    @Modifying
    @Query("UPDATE ChatRoom c SET c.user1Unread = 0 WHERE c.matchId = :matchId AND c.user1Id = :userId")
    void resetUser1Unread(@Param("matchId") String matchId, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE ChatRoom c SET c.user2Unread = 0 WHERE c.matchId = :matchId AND c.user2Id = :userId")
    void resetUser2Unread(@Param("matchId") String matchId, @Param("userId") String userId);
}
