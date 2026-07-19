package com.dating.repository;

import com.dating.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
    Optional<Like> findBySenderIdAndReceiverId(String senderId, String receiverId);
    List<Like> findByReceiverIdAndStatus(String receiverId, Like.LikeStatus status);
    Page<Like> findByReceiverIdAndStatus(String receiverId, Like.LikeStatus status, Pageable pageable);

    @Query("SELECT l FROM Like l WHERE l.senderId = :userId OR l.receiverId = :userId")
    List<Like> findAllByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.receiverId = :userId AND l.status = 'PENDING'")
    long countPendingLikes(@Param("userId") String userId);

    @Query("SELECT l.senderId FROM Like l WHERE l.receiverId = :userId AND l.status = 'PENDING'")
    List<String> findLikerIdsByReceiverId(@Param("userId") String userId);
}
