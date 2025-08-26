package com.PrepWise.controllers;

import com.PrepWise.dto.UpdateProfileRequest;
import com.PrepWise.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody UpdateProfileRequest request) {
        try {
            // Extract token from Authorization header
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Authorization header missing or invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Update profile
            Map<String, Object> response = userService.updateUserProfile(token, request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            if (e.getMessage().contains("Invalid or expired token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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