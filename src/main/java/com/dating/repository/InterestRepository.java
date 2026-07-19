package com.dating.repository;

import com.dating.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, String> {
    Optional<Interest> findByName(String name);
    List<Interest> findByCategory(String category);
}
