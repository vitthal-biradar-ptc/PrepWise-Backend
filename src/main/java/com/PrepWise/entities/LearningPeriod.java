package com.PrepWise.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_periods")
public class LearningPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false, length = 1000)
    private String goal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String focusAreas;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @JsonManagedReference("period-resources")
    @OneToMany(mappedBy = "learningPeriod", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LearningResource> resources = new ArrayList<>();

    @JsonManagedReference("period-tasks")
    @OneToMany(mappedBy = "learningPeriod", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LearningTask> tasks = new ArrayList<>();

    public LearningPeriod() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getFocusAreas() { return focusAreas; }
    public void setFocusAreas(String focusAreas) { this.focusAreas = focusAreas; }

    public LearningPath getLearningPath() { return learningPath; }
    public void setLearningPath(LearningPath learningPath) { this.learningPath = learningPath; }

    public List<LearningResource> getResources() { return resources; }
    public void setResources(List<LearningResource> resources) {
        this.resources.clear();
        if (resources != null) {
            resources.forEach(resource -> resource.setLearningPeriod(this));
            this.resources.addAll(resources);
        }
    }

    public List<LearningTask> getTasks() { return tasks; }
    public void setTasks(List<LearningTask> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            tasks.forEach(task -> task.setLearningPeriod(this));
            this.tasks.addAll(tasks);
        }
    }
}
