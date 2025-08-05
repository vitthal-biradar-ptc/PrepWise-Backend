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
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "portfolioLink", required = false) String portfolioLink) {
        try {
            // Validate required fields
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse("Username is required", HttpStatus.BAD_REQUEST);
            }
            if (email == null || email.trim().isEmpty()) {
                return createErrorResponse("Email is required", HttpStatus.BAD_REQUEST);
            }
            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse("Password is required", HttpStatus.BAD_REQUEST);
            }
            if (name == null || name.trim().isEmpty()) {
                return createErrorResponse("Name is required", HttpStatus.BAD_REQUEST);
            }
            if (location == null || location.trim().isEmpty()) {
                return createErrorResponse("Location is required", HttpStatus.BAD_REQUEST);
            }

            SignUpRequest request = new SignUpRequest();
            request.setUsername(username.trim());
            request.setEmail(email.trim());
            request.setPassword(password);
            request.setName(name.trim());
            request.setLocation(location.trim());
            request.setGithubUrl(githubUrl != null ? githubUrl.trim() : null);
            request.setLinkedinUrl(linkedinUrl != null ? linkedinUrl.trim() : null);
            request.setPortfolioLink(portfolioLink != null ? portfolioLink.trim() : null);

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

    @PostMapping("/login")
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

    @GetMapping("/get-user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid token format");
                error.put("status", HttpStatus.UNAUTHORIZED.value());
                error.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String jwtToken = token.replace("Bearer ", "");
            Map<String, Object> userInfo = userService.getUserFromToken(jwtToken);
            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", HttpStatus.UNAUTHORIZED.value());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
