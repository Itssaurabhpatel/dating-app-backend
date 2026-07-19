package com.dating.repository;

import com.dating.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<NotificationLog> findByUserIdAndIsSentFalse(String userId);
}
