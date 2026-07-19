package com.dating.repository;

import com.dating.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, String> {
    List<ProfilePhoto> findByProfileIdOrderByDisplayOrderAsc(String profileId);
    @Modifying
    @Query("DELETE FROM ProfilePhoto pp WHERE pp.profile.id = :profileId")
    void deleteByProfileId(@Param("profileId") String profileId);
}
