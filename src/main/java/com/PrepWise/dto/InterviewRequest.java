package com.PrepWise.dto;

import java.util.List;

public class InterviewRequest {

    private Long userId;
    private String overallSummary;
    private List<String> strengths;
    private List<String> areasForImprovement;
    private List<QuestionAnalysisDto> questionByQuestionAnalysis;

    public InterviewRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(List<String> areasForImprovement) { this.areasForImprovement = areasForImprovement; }

    public List<QuestionAnalysisDto> getQuestionByQuestionAnalysis() { return questionByQuestionAnalysis; }
    public void setQuestionByQuestionAnalysis(List<QuestionAnalysisDto> questionByQuestionAnalysis) { this.questionByQuestionAnalysis = questionByQuestionAnalysis; }

    public static class QuestionAnalysisDto {
        private String question;
        private String userAnswer;
        private String feedback;
        private Integer score;

        // Getters and Setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
}