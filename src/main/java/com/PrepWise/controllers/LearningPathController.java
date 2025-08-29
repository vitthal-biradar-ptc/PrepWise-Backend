package com.PrepWise.controllers;

import com.PrepWise.dto.LearningPathRequest;
import com.PrepWise.entities.LearningPath;
import com.PrepWise.services.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
@RequiredArgsConstructor
public class LearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateLearningPath(@Valid @RequestBody LearningPathRequest request) {
        try {
            LearningPath learningPath = learningPathService.generateAndStoreLearningPath(request);
            return ResponseEntity.ok(learningPath);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate learning path: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLearningPaths(@PathVariable Long userId) {
        try {
            List<LearningPath> learningPaths = learningPathService.getUserLearningPaths(userId);
            return ResponseEntity.ok(learningPaths);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch learning paths: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{userId}/{pathId}")
    public ResponseEntity<?> deleteLearningPath(@PathVariable Long userId, @PathVariable Long pathId) {
        try {
            learningPathService.deleteLearningPath(userId, pathId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Learning path deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete learning path: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}