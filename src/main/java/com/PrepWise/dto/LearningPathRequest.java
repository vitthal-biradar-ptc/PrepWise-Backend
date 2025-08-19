package com.PrepWise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class LearningPathRequest {

    @NotBlank(message = "Skill is required")
    private String skill;

    @NotBlank(message = "Level is required")
    @Pattern(regexp = "^(Beginner|Intermediate|Advanced)$", message = "Level must be Beginner, Intermediate, or Advanced")
    private String level;

    @NotNull(message = "User ID is required")
    private Long userId;

    public LearningPathRequest() {}

    public LearningPathRequest(String skill, String level, Long userId) {
        this.skill = skill;
        this.level = level;
        this.userId = userId;
    }

    // Getters and Setters
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "LearningPathRequest{" +
                "skill='" + skill + '\'' +
                ", level='" + level + '\'' +
                ", userId=" + userId +
                '}';
    }
}
