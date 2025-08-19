package com.PrepWise.repositories;

import com.PrepWise.entities.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    @Query("SELECT lp FROM LearningPath lp WHERE lp.userId = :userId ORDER BY lp.createdAt DESC")
    List<LearningPath> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
