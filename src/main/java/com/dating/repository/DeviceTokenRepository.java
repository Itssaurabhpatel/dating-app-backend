package com.dating.repository;

import com.dating.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    List<DeviceToken> findByUserIdAndIsActiveTrue(String userId);
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
