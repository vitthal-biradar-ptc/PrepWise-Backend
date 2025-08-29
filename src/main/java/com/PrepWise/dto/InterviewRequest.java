package com.PrepWise.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewRequest {

    private Long userId;
    private String role;
    private String level;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private List<TranscriptDto> transcript;
    private FeedbackDto feedback;
    private Integer overallScore;

    public InterviewRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public List<TranscriptDto> getTranscript() { return transcript; }
    public void setTranscript(List<TranscriptDto> transcript) { this.transcript = transcript; }

    public FeedbackDto getFeedback() { return feedback; }
    public void setFeedback(FeedbackDto feedback) { this.feedback = feedback; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public static class TranscriptDto {
        private String speaker;
        private String text;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getSpeaker() { return speaker; }
        public void setSpeaker(String speaker) { this.speaker = speaker; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class FeedbackDto {
        private String overallSummary;
        private List<String> strengths;
        private List<String> weaknesses;
        private String recommendations;

        // Getters and Setters

        public String getOverallSummary() { return overallSummary; }
        public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; };

        public List<String> getStrengths() { return strengths; }
        public void setStrengths(List<String> strengths) { this.strengths = strengths; }

        public List<String> getWeaknesses() { return weaknesses; }
        public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }


    }
}