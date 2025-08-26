package com.PrepWise.controllers;

import com.PrepWise.config.JwtUtil;
import com.PrepWise.dto.ResumeAnalysisRequest;
import com.PrepWise.dto.ResumeAnalysisResponse;
import com.PrepWise.dto.ResumeParseResponse;
import com.PrepWise.services.GeminiService;
import com.PrepWise.services.PdfService;
import com.PrepWise.services.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyzeResumeController {

    private final GeminiService geminiService;
    private final PdfService pdfService;
    private final UserProfileService userProfileService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/analyze-resume")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
                errorResponse.setDomain("Error");
                errorResponse.setSuggestions(List.of("Please select a PDF file to upload"));
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
                errorResponse.setDomain("Error");
                errorResponse.setSuggestions(List.of("Please upload a PDF file only"));
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String extractedText = pdfService.extractTextFromPdf(file);
            String analysis = geminiService.analyzeResume(extractedText);
            ResumeAnalysisResponse analysisResponse = parseGeminiResponse(analysis);

            return ResponseEntity.ok(analysisResponse);
        } catch (Exception e) {
            ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
            errorResponse.setDomain("Error");
            errorResponse.setSuggestions(List.of("Error processing PDF: " + e.getMessage()));
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/analyze-text")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResumeText(@RequestBody ResumeAnalysisRequest request) {
        try {
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
                errorResponse.setDomain("Error");
                errorResponse.setSuggestions(List.of("Please provide resume text"));
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String analysis = geminiService.analyzeResume(request.getPrompt());
            ResumeAnalysisResponse analysisResponse = parseGeminiResponse(analysis);
            return ResponseEntity.ok(analysisResponse);
        } catch (Exception e) {
            ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
            errorResponse.setDomain("Error");
            errorResponse.setSuggestions(List.of("Error analyzing resume: " + e.getMessage()));
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/parse-resume")
    public ResponseEntity<?> parseResumeAndUpdateProfile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a PDF file to upload");
            }

            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return ResponseEntity.badRequest().body("Please upload a PDF file only");
            }

            // Extract username from JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid or expired token");
            }

            String username = jwtUtil.getUsernameFromToken(token);

            // Extract text from PDF
            String extractedText = pdfService.extractTextFromPdf(file);

            // Parse resume using Gemini
            String parseResponse = geminiService.parseResumeForUserDetails(extractedText);
            ResumeParseResponse resumeData = parseGeminiParseResponse(parseResponse);

            // Update user profile in database
            userProfileService.updateUserProfileFromResume(username, resumeData);

            return ResponseEntity.ok(Collections.singletonMap("message", "Resume parsed and profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing resume: " + e.getMessage());
        }
    }

    private ResumeParseResponse parseGeminiParseResponse(String response) {
        try {
            String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            return objectMapper.readValue(cleanResponse, ResumeParseResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage());
        }
    }

    private ResumeAnalysisResponse parseGeminiResponse(String response) {
        try {
            String cleanResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            return objectMapper.readValue(cleanResponse, ResumeAnalysisResponse.class);
        } catch (Exception e) {
            // Fallback: parse text response manually
            ResumeAnalysisResponse fallbackResponse = new ResumeAnalysisResponse();
            fallbackResponse.setDomain("General");
            fallbackResponse.setSuggestions(Collections.singletonList(response));
            return fallbackResponse;
        }
    }
}
