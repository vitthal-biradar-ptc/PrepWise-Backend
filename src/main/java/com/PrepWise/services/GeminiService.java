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
                    "[Suggestion 1 to enhance the resume]",
                    "[Suggestion 2 to enhance the resume]",
                    "[Suggestion 3 to enhance the resume]",
                    "[Suggestion 4 to enhance the resume]",
                    "[Suggestion 5 to enhance the resume]",
                    "[Suggestion 6 to enhance the resume]",
                    "[Suggestion 7 to enhance the resume]"
                  ]
                }
                
                Resume Text:
                """ + resumeText;
    }

    public String parseResumeForUserDetails(String resumeText) {
        String parsePrompt = createResumeParsePrompt(resumeText);
        return askGemini(parsePrompt);
    }

    private String createResumeParsePrompt(String resumeText) {
        return """
                Please extract domain, skills, certifications, and achievements from the following resume and provide a structured response in JSON format only:
                
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
                  ]
                }
                
                Guidelines:
                - For domain: Pick the SINGLE most relevant domain from the list above. Do NOT combine domains or create custom ones. If the person has multiple skills, choose the PRIMARY domain they are most suited for.
                - For skills, infer proficiency level based on experience, projects, or mentions
                - Extract only legitimate certifications with clear issuers
                - Include notable achievements, awards, recognitions, or significant accomplishments
                - Use "Unknown" for dates if not specified
                - If any category has no data, return an empty array
                - Only get top 10 skills, top 3 certifications, and top 3 achievements
                
                Resume Text:
                """ + resumeText;
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
