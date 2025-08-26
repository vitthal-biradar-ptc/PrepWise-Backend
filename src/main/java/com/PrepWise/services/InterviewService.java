package com.PrepWise.services;

import com.PrepWise.dto.InterviewRequest;
import com.PrepWise.entities.Interview;
import com.PrepWise.entities.QuestionAnalysis;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.InterviewRepository;
import com.PrepWise.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Interview saveInterview(InterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Interview interview = new Interview();
        interview.setUser(user);
        interview.setOverallSummary(request.getOverallSummary());
        interview.setStrengths(request.getStrengths());
        interview.setAreasForImprovement(request.getAreasForImprovement());

        // Convert DTO to entities
        List<QuestionAnalysis> questionAnalyses = request.getQuestionByQuestionAnalysis().stream()
                .map(dto -> {
                    QuestionAnalysis qa = new QuestionAnalysis();
                    qa.setQuestion(dto.getQuestion());
                    qa.setUserAnswer(dto.getUserAnswer());
                    qa.setFeedback(dto.getFeedback());
                    qa.setScore(dto.getScore());
                    qa.setInterview(interview);
                    return qa;
                }).collect(Collectors.toList());

        interview.setQuestionByQuestionAnalysis(questionAnalyses);

        return interviewRepository.save(interview);
    }

    public List<Interview> getInterviewsByUserId(Long userId) {
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + id));
    }
}