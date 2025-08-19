package com.PrepWise.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "learning_resources")
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String type;

    @JsonBackReference("period-resources")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_period_id", nullable = false)
    private LearningPeriod learningPeriod;

    public LearningResource() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LearningPeriod getLearningPeriod() { return learningPeriod; }
    public void setLearningPeriod(LearningPeriod learningPeriod) { this.learningPeriod = learningPeriod; }
}
