package com.PrepWise.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient geminiWebClient;
    private final String geminiApiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askGemini(String prompt) {
        try {
            Map<String, Object> requestBody = createGeminiRequest(prompt);

            String response = geminiWebClient.post()
                    .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String analyzeResume(String resumeText) {
        String analysisPrompt = createResumeAnalysisPrompt(resumeText);
        return askGemini(analysisPrompt);
    }

    private String createResumeAnalysisPrompt(String resumeText) {
        return """
                Please analyze the following resume and provide a structured response in JSON format only:
                
                Return your response in this exact JSON format (no markdown, no code blocks):
                {
                  "domain": "[Primary domain like Full Stack, DevOps, AI/ML, Blockchain, Data Science, etc.]",
                  "suggestions": [
                    "[Brief actionable suggestion 1 (max 200 characters)]",
                    "[Brief actionable suggestion 2 (max 200 characters)]",
                    "[Brief actionable suggestion 3 (max 200 characters)]",
                    "[Brief actionable suggestion 4 (max 200 characters)]",
                    "[Brief actionable suggestion 5 (max 200 characters)]",
                    "[Brief actionable suggestion 6 (max 200 characters)]",
                    "[Brief actionable suggestion 7 (max 200 characters)]"
                  ]
                }
                
                Guidelines for suggestions:
                - Keep each suggestion under 200 characters
                - Focus on specific, actionable improvements
                - Prioritize high-impact changes
                - Use concise, direct language
                - Examples: "Add quantified achievements", "Include relevant keywords", "Highlight leadership experience"
                
                Resume Text:
                """ + resumeText;
    }

    public String parseResumeForUserDetails(String resumeText) {
        String parsePrompt = createResumeParsePrompt(resumeText);
        return askGemini(parsePrompt);
    }

    private String createResumeParsePrompt(String resumeText) {
        return """
                Please extract domain, skills, certifications, achievements, and domain distribution from the following resume and provide a structured response in JSON format only:
                
                Return your response in this exact JSON format (no markdown, no code blocks):
                {
                  "domain": "[Primary domain - choose ONE from: Full Stack Development, Frontend Development, Backend Development, Mobile Development, DevOps Engineering, Cloud Engineering, Data Science, Machine Learning Engineering, Artificial Intelligence, Cybersecurity, Quality Assurance, UI/UX Design, Game Development, Blockchain Development, Software Architecture, Database Administration, Network Engineering, System Administration, Product Management, Technical Writing]",
                  "skills": [
                    {
                      "name": "[Skill name]",
                      "proficiency": "[Beginner/Intermediate/Advanced/Expert]"
                    }
                  ],
                  "certifications": [
                    {
                      "name": "[Certification name]",
                      "issuer": "[Issuing organization]",
                      "date": "[Date or year obtained]"
                    }
                  ],
                  "achievements": [
                    {
                      "name": "[Achievement title]",
                      "description": "[Brief description]",
                      "date": "[Date or year achieved]"
                    }
                  ],
                  "domainDistribution": {
                    "labels": ["[Domain1]", "[Domain2]", "[Domain3]", "[Domain4]", "[Domain5]"],
                    "data": [30, 25, 20, 15, 10]
                  }
                }
                
                Guidelines:
                - For domain: Pick the SINGLE most relevant domain from the list above. Do NOT combine domains or create custom ones. If the person has multiple skills, choose the PRIMARY domain they are most suited for.
                - For skills, infer proficiency level based on experience, projects, or mentions
                - Extract only legitimate certifications with clear issuers
                - Include notable achievements, awards, recognitions, or significant accomplishments
                - Use "Unknown" for dates if not specified
                - If any category has no data, return an empty array
                - Only get top 10 skills, top 3 certifications, and top 3 achievements
                - For domainDistribution: Analyze the resume and identify the top 5 domains the person has experience in. Use these specific domain names: Web Development, Mobile App Development, DevOps, Data Science, Machine Learning, Artificial Intelligence, Cybersecurity, Cloud Computing, UI/UX Design, Game Development, Blockchain, Database Management, Network Engineering, Quality Assurance, Technical Writing, Product Management, Software Architecture, System Administration. The percentages should add up to 100 and represent the person's skill distribution across these domains based on their resume content.
                
                Resume Text:
                """ + resumeText;
    }

    public String generateLearningPath(String skill, String level) {
        String learningPrompt = createLearningPathPrompt(skill, level);
        return askGemini(learningPrompt);
    }

    private String createLearningPathPrompt(String skill, String level) {
        return """
                You are an expert career and learning path generator.
                I will provide you with:
                1. The skill they want to learn or improve: %s
                2. The target proficiency level they want to reach: %s
                3. The preferred duration of the plan: %s

                Your task is to generate a structured **learning path** for that skill in JSON format only.
                Return your response in this exact JSON format (no markdown, no code blocks):
                {
                   "duration": "[short-term | medium-term | long-term]",
                   "learningPath": [
                     {
                       "period": "[e.g., Week 1-2, Month 1, Phase 1]",
                       "goal": "[Specific learning goal for this period]",
                       "focusAreas": [
                         "[Focus area 1]",
                         "[Focus area 2]",
                         "[Focus area 3]"
                       ],
                       "resources": [
                         {
                           "title": "[Resource title]",
                           "url": "[Resource URL]",
                           "type": "[course | documentation | video | article | tutorial]"
                         }
                       ],
                       "tasks": [
                         {
                           "id": "[Unique task ID]",
                           "description": "[Task description]",
                           "completed": false,
                           "estimatedHours": 0
                         }
                       ]
                     }
                   ]
                 }
                ### Rules:
                - Always output valid JSON that matches this schema exactly.
                - The `duration` must be based on the user's choice.
                - Each `learningPath` entry should represent a clear step (e.g., Week 1, Month 1, Phase 1).
                - Include at least 3–5 focus areas per period.
                - Provide **realistic and widely recognized resources** (e.g., MDN, freeCodeCamp, Coursera, official docs, YouTube tutorials).
                - Each period must have at least 3 tasks with unique IDs (`task-1`, `task-2`, etc.).
                - Set all tasks' `completed` fields to `false`.
                - Estimate task hours realistically depending on complexity.
                - Do not include explanations, comments, or extra text outside of JSON.
                """+ skill + level;
    }

    private Map<String, Object> createGeminiRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", List.of(part));
        request.put("contents", List.of(content));

        return request;
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
