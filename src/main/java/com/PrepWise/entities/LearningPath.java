package com.PrepWise.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skill;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JsonManagedReference
    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LearningPeriod> learningPeriods = new ArrayList<>();

    // Hide User object from JSON serialization completely
    @JsonIgnore
    @Transient
    private User user;

    public LearningPath() {
        this.createdAt = LocalDateTime.now();
    }

    public LearningPath(String skill, String level, User user) {
        this.skill = skill;
        this.level = level;
        this.user = user;
        this.userId = user.getId();
        this.createdAt = LocalDateTime.now();
    }

    public LearningPath(String skill, String level, String duration, User user) {
        this.skill = skill;
        this.level = level;
        this.duration = duration;
        this.user = user;
        this.userId = user.getId();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public List<LearningPeriod> getLearningPeriods() { return learningPeriods; }
    public void setLearningPeriods(List<LearningPeriod> learningPeriods) {
        this.learningPeriods.clear();
        if (learningPeriods != null) {
            learningPeriods.forEach(period -> period.setLearningPath(this));
            this.learningPeriods.addAll(learningPeriods);
        }
    }
}