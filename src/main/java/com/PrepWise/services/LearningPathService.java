package com.PrepWise.services;

import com.PrepWise.dto.LearningPathRequest;
import com.PrepWise.entities.LearningPath;
import com.PrepWise.entities.LearningPeriod;
import com.PrepWise.entities.LearningResource;
import com.PrepWise.entities.LearningTask;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.LearningPathRepository;
import com.PrepWise.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public LearningPath generateAndStoreLearningPath(LearningPathRequest request) {
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Generate learning path from Gemini (let Gemini determine the duration)
        String geminiResponse = geminiService.generateLearningPath(
                request.getSkill(),
                request.getLevel()
        );

        // Parse and store learning path
        LearningPath learningPath = parseLearningPathResponse(geminiResponse, request, user);

        // Save and return the learning path
        LearningPath savedPath = learningPathRepository.save(learningPath);

        // Clear the transient user field to ensure it's not serialized
        savedPath.setUser(null);

        return savedPath;
    }

    private LearningPath parseLearningPathResponse(String response, LearningPathRequest request, User user) {
        try {
            // Clean the response to ensure it's valid JSON
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonNode rootNode = objectMapper.readTree(cleanResponse);

            // Extract duration from Gemini response
            String duration = rootNode.has("duration") ? rootNode.get("duration").asText() : "medium-term";

            LearningPath learningPath = new LearningPath(
                    request.getSkill(),
                    request.getLevel(),
                    duration,
                    user
            );

            // Parse learning periods
            JsonNode learningPathArray = rootNode.get("learningPath");
            if (learningPathArray != null && learningPathArray.isArray()) {
                List<LearningPeriod> periods = new ArrayList<>();

                for (JsonNode periodNode : learningPathArray) {
                    LearningPeriod period = createLearningPeriod(periodNode, learningPath);
                    periods.add(period);
                }

                learningPath.setLearningPeriods(periods);
            }

            return learningPath;
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + response);
            throw new RuntimeException("Failed to parse learning path response: " + e.getMessage(), e);
        }
    }

    private LearningPeriod createLearningPeriod(JsonNode periodNode, LearningPath learningPath) {
        try {
            LearningPeriod period = new LearningPeriod();

            // Set basic fields
            period.setPeriod(periodNode.get("period").asText());
            period.setGoal(periodNode.get("goal").asText());
            period.setLearningPath(learningPath);

            // Set focus areas as JSON string
            JsonNode focusAreasNode = periodNode.get("focusAreas");
            if (focusAreasNode != null) {
                period.setFocusAreas(objectMapper.writeValueAsString(focusAreasNode));
            }

            // Create resources and tasks
            List<LearningResource> resources = createResources(periodNode.get("resources"), period);
            List<LearningTask> tasks = createTasks(periodNode.get("tasks"), period);

            period.setResources(resources);
            period.setTasks(tasks);

            return period;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create learning period: " + e.getMessage(), e);
        }
    }

    private List<LearningResource> createResources(JsonNode resourcesNode, LearningPeriod period) {
        List<LearningResource> resources = new ArrayList<>();
        if (resourcesNode != null && resourcesNode.isArray()) {
            try {
                for (JsonNode resourceNode : resourcesNode) {
                    LearningResource resource = new LearningResource();
                    resource.setTitle(resourceNode.get("title").asText());
                    resource.setUrl(resourceNode.get("url").asText());
                    resource.setType(resourceNode.get("type").asText());
                    resource.setLearningPeriod(period);
                    resources.add(resource);
                }
            } catch (Exception e) {
                System.err.println("Error creating resources: " + e.getMessage());
            }
        }
        return resources;
    }

    private List<LearningTask> createTasks(JsonNode tasksNode, LearningPeriod period) {
        List<LearningTask> tasks = new ArrayList<>();
        if (tasksNode != null && tasksNode.isArray()) {
            try {
                for (JsonNode taskNode : tasksNode) {
                    LearningTask task = new LearningTask();
                    task.setTaskId(taskNode.get("id").asText());
                    task.setDescription(taskNode.get("description").asText());
                    task.setCompleted(taskNode.get("completed").asBoolean());
                    task.setEstimatedHours(taskNode.get("estimatedHours").asInt());
                    task.setLearningPeriod(period);
                    tasks.add(task);
                }
            } catch (Exception e) {
                System.err.println("Error creating tasks: " + e.getMessage());
            }
        }
        return tasks;
    }

    public List<LearningPath> getUserLearningPaths(Long userId) {
        List<LearningPath> learningPaths = learningPathRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // Clear transient user fields
        learningPaths.forEach(path -> path.setUser(null));
        return learningPaths;
    }

    @Transactional
    public void deleteLearningPath(Long userId, Long pathId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        // Find the learning path
        LearningPath learningPath = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Learning path not found with ID: " + pathId));

        // Verify that the learning path belongs to the user
        if (!learningPath.getUserId().equals(userId)) {
            throw new RuntimeException("Learning path does not belong to the specified user");
        }

        // Delete the learning path (cascade will handle related entities)
        learningPathRepository.delete(learningPath);
    }
}
