package com.PrepWise.controllers;

import com.PrepWise.dto.InterviewRequest;
import com.PrepWise.entities.Interview;
import com.PrepWise.services.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping("/save")
    public ResponseEntity<Interview> saveInterview(@RequestBody InterviewRequest request) {
        Interview savedInterview = interviewService.saveInterview(request);
        return ResponseEntity.ok(savedInterview);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Interview>> getUserInterviews(@PathVariable Long userId) {
        List<Interview> interviews = interviewService.getInterviewsByUserId(userId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/user/{userId}/report/{id}")
    public ResponseEntity<Interview> getInterview(@PathVariable Long id) {
        Interview interview = interviewService.getInterviewById(id);
        return ResponseEntity.ok(interview);
    }
}