package com.dating.repository;

import com.dating.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByMatchIdAndIsDeletedFalseOrderByCreatedAtDesc(String matchId, Pageable pageable);
    List<Message> findByMatchIdAndIsReadFalseAndSenderIdNot(String matchId, String senderId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :now WHERE m.matchId = :matchId AND m.senderId != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("matchId") String matchId, @Param("userId") String userId, @Param("now") Instant now);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.matchId = :matchId AND m.senderId != :userId AND m.isRead = false")
    long countUnreadMessages(@Param("matchId") String matchId, @Param("userId") String userId);
}
