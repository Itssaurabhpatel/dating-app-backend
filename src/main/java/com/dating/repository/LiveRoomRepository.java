package com.dating.repository;

import com.dating.entity.LiveRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveRoomRepository extends JpaRepository<LiveRoom, String> {
    List<LiveRoom> findByType(String type);
}
