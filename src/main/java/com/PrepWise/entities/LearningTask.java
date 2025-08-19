package com.PrepWise.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "learning_tasks")
public class LearningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskId;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Integer estimatedHours;

    @JsonBackReference("period-tasks")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_period_id", nullable = false)
    private LearningPeriod learningPeriod;

    public LearningTask() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public LearningPeriod getLearningPeriod() { return learningPeriod; }
    public void setLearningPeriod(LearningPeriod learningPeriod) { this.learningPeriod = learningPeriod; }
}
