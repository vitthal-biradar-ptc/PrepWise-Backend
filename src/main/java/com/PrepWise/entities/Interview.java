package com.PrepWise.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionAnalysis> questionByQuestionAnalysis;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Interview() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(List<String> areasForImprovement) { this.areasForImprovement = areasForImprovement; }

    public List<QuestionAnalysis> getQuestionByQuestionAnalysis() { return questionByQuestionAnalysis; }
    public void setQuestionByQuestionAnalysis(List<QuestionAnalysis> questionByQuestionAnalysis) { this.questionByQuestionAnalysis = questionByQuestionAnalysis; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}