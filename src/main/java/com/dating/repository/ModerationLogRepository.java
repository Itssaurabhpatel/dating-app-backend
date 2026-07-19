package com.dating.repository;

import com.dating.entity.ModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, String> {
    Page<ModerationLog> findByModeratorId(String moderatorId, Pageable pageable);
}
