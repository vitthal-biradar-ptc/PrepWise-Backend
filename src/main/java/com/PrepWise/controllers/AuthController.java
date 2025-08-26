package com.PrepWise.controllers;

import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpRequest request) {
        try {
            // Validate required fields
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return createErrorResponse("Username is required", HttpStatus.BAD_REQUEST);
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return createErrorResponse("Email is required", HttpStatus.BAD_REQUEST);
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return createErrorResponse("Password is required", HttpStatus.BAD_REQUEST);
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return createErrorResponse("Name is required", HttpStatus.BAD_REQUEST);
            }
            if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                return createErrorResponse("Location is required", HttpStatus.BAD_REQUEST);
            }

            // Trim the values
            request.setUsername(request.getUsername().trim());
            request.setEmail(request.getEmail().trim());
            request.setName(request.getName().trim());
            request.setLocation(request.getLocation().trim());
            if (request.getGithubUrl() != null) {
                request.setGithubUrl(request.getGithubUrl().trim());
            }
            if (request.getLinkedinUrl() != null) {
                request.setLinkedinUrl(request.getLinkedinUrl().trim());
            }
            if (request.getPortfolioLink() != null) {
                request.setPortfolioLink(request.getPortfolioLink().trim());
            }

            AuthResponse response = userService.registerUser(request);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("token", response.getToken());
            successResponse.put("tokenType", response.getTokenType());
            successResponse.put("message", "User registered successfully");
            successResponse.put("status", HttpStatus.CREATED.value());
            successResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
        } catch (RuntimeException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return createErrorResponse("An unexpected error occurred during registration", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", status.value());
        error.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(error);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", HttpStatus.UNAUTHORIZED.value());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = userService.validateToken(token.replace("Bearer ", ""));
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            error.put("valid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
