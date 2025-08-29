package com.PrepWise.services;

import com.PrepWise.dto.InterviewRequest;
import com.PrepWise.entities.Interview;
import com.PrepWise.entities.TranscriptEntry;
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
        interview.setRole(request.getRole());
        interview.setLevel(request.getLevel());
        interview.setStartTime(request.getStartTime());
        interview.setEndTime(request.getEndTime());
        interview.setDuration(request.getDuration());
        interview.setOverallScore(request.getOverallScore());

        // Map feedback
        if (request.getFeedback() != null) {
            interview.setOverallSummary(request.getFeedback().getOverallSummary());
            interview.setStrengths(request.getFeedback().getStrengths());
            interview.setAreasForImprovement(request.getFeedback().getWeaknesses());
            interview.setRecommendations(request.getFeedback().getRecommendations());
        }

        // Convert transcript DTOs to entities
        if (request.getTranscript() != null) {
            List<TranscriptEntry> transcriptEntries = request.getTranscript().stream()
                    .map(dto -> {
                        TranscriptEntry entry = new TranscriptEntry();
                        entry.setSpeaker(dto.getSpeaker());
                        entry.setText(dto.getText());
                        entry.setTimestamp(dto.getTimestamp());
                        entry.setInterview(interview);
                        return entry;
                    }).collect(Collectors.toList());
            interview.setTranscript(transcriptEntries);
        }

        Interview savedInterview = interviewRepository.save(interview);

        savedInterview.setUser(null);
        return savedInterview;
    }

    public List<Interview> getInterviewsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        interviews.forEach(interview -> interview.setUser(null));
        return interviews;
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + id));
    }
}