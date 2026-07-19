package com.dating.repository;

import com.dating.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {
    List<BlockedUser> findByUserId(String userId);
    Optional<BlockedUser> findByUserIdAndBlockedUserId(String userId, String blockedUserId);
    boolean existsByUserIdAndBlockedUserId(String userId, String blockedUserId);
}
