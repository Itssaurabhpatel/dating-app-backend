package com.dating.repository;

import com.dating.entity.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, String> {
    Optional<Swipe> findByUserIdAndTargetId(String userId, String targetId);
    long countByUserIdAndCreatedAtAfter(String userId, java.time.Instant since);
}
