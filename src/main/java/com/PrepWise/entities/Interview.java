package com.PrepWise.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role")
    private String role;

    @Column(name = "level")
    private String level;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TranscriptEntry> transcript;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(columnDefinition = "TEXT")
    private String overallSummary;

    @ElementCollection
    @CollectionTable(name = "interview_strengths", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "strength")
    private List<String> strengths;

    @ElementCollection
    @CollectionTable(name = "interview_improvements", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "improvement")
    private List<String> areasForImprovement;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionAnalysis> questionByQuestionAnalysis;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonIgnore
    @Transient
    private User user;


    public Interview() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public List<TranscriptEntry> getTranscript() { return transcript; }
    public void setTranscript(List<TranscriptEntry> transcript) { this.transcript = transcript; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(List<String> areasForImprovement) { this.areasForImprovement = areasForImprovement; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public List<QuestionAnalysis> getQuestionByQuestionAnalysis() { return questionByQuestionAnalysis; }
    public void setQuestionByQuestionAnalysis(List<QuestionAnalysis> questionByQuestionAnalysis) { this.questionByQuestionAnalysis = questionByQuestionAnalysis; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}