package com.dating.repository;

import com.dating.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    @Query("SELECT m FROM Match m WHERE (m.user1Id = :userId OR m.user2Id = :userId) AND m.isActive = true")
    List<Match> findActiveMatchesByUserId(@Param("userId") String userId);

    @Query("SELECT m FROM Match m WHERE (m.user1Id = :userId OR m.user2Id = :userId) AND m.isActive = true")
    Page<Match> findActiveMatchesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT m FROM Match m WHERE ((m.user1Id = :user1 AND m.user2Id = :user2) OR (m.user1Id = :user2 AND m.user2Id = :user1)) AND m.isActive = true")
    Optional<Match> findActiveMatchBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT m FROM Match m WHERE (m.user1Id = :userId OR m.user2Id = :userId) AND m.isActive = false ORDER BY m.unmatchedAt DESC")
    Page<Match> findHistoryByUserId(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Match m SET m.isActive = false, m.unmatchedAt = :now WHERE m.id = :matchId")
    void unmatch(@Param("matchId") String matchId, @Param("now") Instant now);

    @Query("SELECT m FROM Match m WHERE m.id = :matchId AND (m.user1Id = :userId OR m.user2Id = :userId)")
    Optional<Match> findByIdAndUserId(@Param("matchId") String matchId, @Param("userId") String userId);
}
