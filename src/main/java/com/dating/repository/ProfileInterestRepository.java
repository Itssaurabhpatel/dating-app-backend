package com.dating.repository;

import com.dating.entity.ProfileInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileInterestRepository extends JpaRepository<ProfileInterest, String> {
    List<ProfileInterest> findByProfileId(String profileId);
    @Modifying
    @Query("DELETE FROM ProfileInterest pi WHERE pi.profile.id = :profileId")
    void deleteByProfileId(@Param("profileId") String profileId);
}
