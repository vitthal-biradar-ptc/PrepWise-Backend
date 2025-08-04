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
