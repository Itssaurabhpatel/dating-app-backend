package com.dating.repository;

import com.dating.entity.Profile;
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
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUserId(String userId);
    boolean existsByUserId(String userId);

    @Query("SELECT p FROM Profile p WHERE p.gender = :interestedIn AND p.interestedIn = :gender " +
           "AND p.isVisible = true AND p.userId != :userId " +
           "AND p.latitude BETWEEN :minLat AND :maxLat " +
           "AND p.longitude BETWEEN :minLon AND :maxLon")
    List<Profile> findPotentialMatches(
            @Param("userId") String userId,
            @Param("gender") Profile.Gender gender,
            @Param("interestedIn") Profile.Gender interestedIn,
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLon") double minLon, @Param("maxLon") double maxLon,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Profile p SET p.lastActive = :timestamp WHERE p.userId = :userId")
    void updateLastActive(@Param("userId") String userId, @Param("timestamp") Instant timestamp);

    @Query("SELECT p FROM Profile p WHERE p.userId IN :userIds")
    List<Profile> findByUserIds(@Param("userIds") List<String> userIds);
}
