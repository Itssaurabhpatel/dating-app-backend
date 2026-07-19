package com.dating.repository;

import com.dating.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByAppleId(String appleId);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastActive = :timestamp WHERE u.id = :userId")
    void updateLastActive(@Param("userId") String userId, @Param("timestamp") Instant timestamp);

    @Modifying
    @Query("UPDATE User u SET u.status = 'DELETED', u.deletedAt = :timestamp WHERE u.id = :userId")
    void softDelete(@Param("userId") String userId, @Param("timestamp") Instant timestamp);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.status = 'ACTIVE'")
    Optional<User> findActiveById(@Param("userId") String userId);
}
