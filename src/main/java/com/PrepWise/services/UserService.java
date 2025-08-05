package com.PrepWise.services;

import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.UserRepository;
import com.PrepWise.config.JwtUtil;
import com.PrepWise.utils.FileUploadService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileUploadService fileUploadService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       FileUploadService fileUploadService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.fileUploadService = fileUploadService;
    }

    public AuthResponse registerUser(SignUpRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Location is required");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        try {
            // Generate dummy profile photo URL
            String profilePhotoUrl = fileUploadService.generateDefaultAvatar(request.getEmail());
            System.out.println("Generated dummy avatar for user " + request.getEmail() + ": " + profilePhotoUrl);

            // Create new user
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setEmail(request.getEmail().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName().trim());
            user.setProfilePhoto(profilePhotoUrl);
            user.setLocation(request.getLocation().trim());
            user.setGithubUrl(request.getGithubUrl());
            user.setLinkedinUrl(request.getLinkedinUrl());
            user.setPortfolioLink(request.getPortfolioLink());

            User savedUser = userRepository.save(user);

            // Generate token
            String token = jwtUtil.generateToken(savedUser.getUsername());
            return new AuthResponse(token);

        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }

    public AuthResponse loginUser(LoginRequest request) {
        // Validate input
        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().trim().isEmpty()) {
            throw new RuntimeException("Username or email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        try {
            String usernameOrEmail = request.getUsernameOrEmail().trim();
            Optional<User> userOpt;

            // First try to find by username
            userOpt = userRepository.findByUsername(usernameOrEmail);

            // If not found by username, try by email
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(usernameOrEmail);
            }

            if (userOpt.isEmpty()) {
                throw new RuntimeException("Invalid credentials");
            }

            User user = userOpt.get();

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            // Generate token
            String token = jwtUtil.generateToken(user.getUsername());
            return new AuthResponse(token);

        } catch (Exception e) {
            throw new RuntimeException("Error logging in: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public Map<String, Object> getUserFromToken(String token) {
        try {
            // Validate token first
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Invalid or expired token");
            }

            // Extract username from token
            String username = jwtUtil.getUsernameFromToken(token);

            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOpt.get();

            // Prepare complete user profile response in exact format
            Map<String, Object> response = new HashMap<>();

            // User basic info
            response.put("profilePhoto", user.getProfilePhoto());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("domainBadge", user.getDomainBadge() != null ? user.getDomainBadge() : "Software Development");
            response.put("location", user.getLocation());
            response.put("githubUrl", user.getGithubUrl());
            response.put("linkedinUrl", user.getLinkedinUrl());
            response.put("portfolioLink", user.getPortfolioLink());

            // Skills
            List<Map<String, Object>> skills = user.getSkills().stream()
                    .map(skill -> {
                        Map<String, Object> skillMap = new HashMap<>();
                        skillMap.put("id", skill.getId());
                        skillMap.put("name", skill.getName());
                        skillMap.put("proficiency", skill.getProficiency());
                        return skillMap;
                    })
                    .collect(Collectors.toList());
            response.put("skills", skills);

            // Certifications
            List<Map<String, Object>> certifications = user.getCertifications().stream()
                    .map(cert -> {
                        Map<String, Object> certMap = new HashMap<>();
                        certMap.put("id", cert.getId());
                        certMap.put("name", cert.getName());
                        certMap.put("issuer", cert.getIssuer());
                        certMap.put("date", cert.getDate());
                        return certMap;
                    })
                    .collect(Collectors.toList());
            response.put("certifications", certifications);

            // Achievements
            List<Map<String, Object>> achievements = user.getAchievements().stream()
                    .map(achievement -> {
                        Map<String, Object> achievementMap = new HashMap<>();
                        achievementMap.put("id", achievement.getId());
                        achievementMap.put("name", achievement.getName());
                        achievementMap.put("description", achievement.getDescription());
                        achievementMap.put("date", achievement.getDate());
                        return achievementMap;
                    })
                    .collect(Collectors.toList());
            response.put("achievements", achievements);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user from token: " + e.getMessage());
        }
    }
}
