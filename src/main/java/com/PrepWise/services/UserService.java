package com.PrepWise.services;

import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.dto.UpdateProfileRequest;
import com.PrepWise.entities.User;
import com.PrepWise.entities.Achievement;
import com.PrepWise.entities.Certification;
import com.PrepWise.entities.Skill;
import com.PrepWise.repositories.UserRepository;
import com.PrepWise.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil
                      ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
            String profilePhotoUrl = generateDefaultAvatar(request.getEmail());
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
    }public String generateDefaultAvatar(String email) {
        if (email != null && !email.isEmpty()) {
            return "https://i.pravatar.cc/150?u=" + Math.abs(email.hashCode());
        }
        return "https://i.pravatar.cc/150?u=" + System.currentTimeMillis();
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

            // Domain distribution data
            if (user.getDomainDistribution() != null && !user.getDomainDistribution().trim().isEmpty()) {
                try {
                    Map<String, Object> domainData = objectMapper.readValue(user.getDomainDistribution(), Map.class);
                    response.put("domainData", domainData);
                } catch (Exception e) {
                    // Fallback if parsing fails
                    Map<String, Object> fallbackDomainData = new HashMap<>();
                    fallbackDomainData.put("labels", List.of("Software Development"));
                    fallbackDomainData.put("datasets", List.of(Map.of("data", List.of(100))));
                    response.put("domainData", fallbackDomainData);
                }
            } else {
                // Default domain data if not set
                Map<String, Object> defaultDomainData = new HashMap<>();
                defaultDomainData.put("labels", List.of("Software Development"));
                defaultDomainData.put("datasets", List.of(Map.of("data", List.of(100))));
                response.put("domainData", defaultDomainData);
            }

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

    @Transactional
    public Map<String, Object> updateUserProfile(String token, UpdateProfileRequest request) {
        try {
            // Validate token
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

            // Update basic user information
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                user.setName(request.getName().trim());
            }
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                user.setEmail(request.getEmail().trim());
            }
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                user.setLocation(request.getLocation().trim());
            }
            if (request.getProfilePhoto() != null && !request.getProfilePhoto().trim().isEmpty()) {
                user.setProfilePhoto(request.getProfilePhoto().trim());
            }
            if (request.getDomainBadge() != null && !request.getDomainBadge().trim().isEmpty()) {
                user.setDomainBadge(request.getDomainBadge().trim());
            }

            // Update URLs
            user.setGithubUrl(request.getGithubUrl());
            user.setLinkedinUrl(request.getLinkedinUrl());
            user.setPortfolioLink(request.getPortfolioLink());

            // Update domain distribution
            if (request.getDomainData() != null) {
                try {
                    String domainDistributionJson = objectMapper.writeValueAsString(request.getDomainData());
                    user.setDomainDistribution(domainDistributionJson);
                } catch (Exception e) {
                    System.err.println("Error serializing domain distribution: " + e.getMessage());
                }
            }

            // Clear existing related entities
            user.getSkills().clear();
            user.getCertifications().clear();
            user.getAchievements().clear();

            // Update skills
            if (request.getSkills() != null) {
                List<Skill> skills = request.getSkills().stream()
                        .map(skillDto -> {
                            Skill skill = new Skill(skillDto.getName(), skillDto.getProficiency());
                            skill.setUser(user);
                            return skill;
                        })
                        .collect(Collectors.toList());
                user.setSkills(skills);
            }

            // Update certifications
            if (request.getCertifications() != null) {
                List<Certification> certifications = request.getCertifications().stream()
                        .map(certDto -> {
                            Certification cert = new Certification(certDto.getName(), certDto.getIssuer(), certDto.getDate());
                            cert.setUser(user);
                            return cert;
                        })
                        .collect(Collectors.toList());
                user.setCertifications(certifications);
            }

            // Update achievements
            if (request.getAchievements() != null) {
                List<Achievement> achievements = request.getAchievements().stream()
                        .map(achDto -> {
                            Achievement achievement = new Achievement(achDto.getName(), achDto.getDescription(), achDto.getDate());
                            achievement.setUser(user);
                            return achievement;
                        })
                        .collect(Collectors.toList());
                user.setAchievements(achievements);
            }

            // Save updated user
            User savedUser = userRepository.save(user);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error updating user profile: " + e.getMessage());
        }
    }
}
