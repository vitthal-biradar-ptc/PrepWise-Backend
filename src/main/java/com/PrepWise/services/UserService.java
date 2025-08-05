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
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("profilePhoto", user.getProfilePhoto());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("domainBadge", user.getDomainBadge() != null ? user.getDomainBadge() : "Full Stack Developer");
            userInfo.put("location", user.getLocation());
            userInfo.put("githubUrl", user.getGithubUrl());
            userInfo.put("linkedinUrl", user.getLinkedinUrl());
            userInfo.put("portfolioLink", user.getPortfolioLink());

            response.put("user", userInfo);

            // Charts data - exact format as specified
            Map<String, Object> charts = new HashMap<>();

            // Domain data chart
            Map<String, Object> domainData = new HashMap<>();
            domainData.put("labels", List.of("Web Development", "Mobile App Development", "DevOps", "Data Science", "CyberSecurity", "Cloud Engineer"));
            Map<String, Object> domainDataset = new HashMap<>();
            domainDataset.put("data", List.of(30, 20, 15, 10, 10, 15));
            domainData.put("datasets", List.of(domainDataset));

            // ATS data chart
            Map<String, Object> atsData = new HashMap<>();
            atsData.put("labels", List.of("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"));
            Map<String, Object> atsDataset = new HashMap<>();
            atsDataset.put("label", "ATS Score");
            atsDataset.put("data", List.of(65, 59, 80, 81, 56, 55));
            atsDataset.put("fill", false);
            atsDataset.put("borderColor", "#42A5F5");
            atsDataset.put("tension", 0.4);
            atsData.put("datasets", List.of(atsDataset));

            charts.put("domainData", domainData);
            charts.put("atsData", atsData);
            response.put("charts", charts);

            // Skills - exact format as specified
            List<Map<String, String>> skills = user.getSkills() != null && !user.getSkills().isEmpty() ?
                user.getSkills().stream().map(skill -> {
                    Map<String, String> skillMap = new HashMap<>();
                    skillMap.put("name", skill.getName());
                    skillMap.put("proficiency", skill.getProficiency());
                    return skillMap;
                }).collect(Collectors.toList()) :
                List.of(
                    Map.of("name", "Angular", "proficiency", "Advanced"),
                    Map.of("name", "React", "proficiency", "Intermediate"),
                    Map.of("name", "Node.js", "proficiency", "Advanced"),
                    Map.of("name", "Express", "proficiency", "Intermediate"),
                    Map.of("name", "TypeScript", "proficiency", "Advanced"),
                    Map.of("name", "PrimeNG", "proficiency", "Beginner"),
                    Map.of("name", "MongoDB", "proficiency", "Intermediate"),
                    Map.of("name", "AWS", "proficiency", "Beginner")
                );
            response.put("skills", skills);

            // Certifications - exact format as specified
            List<Map<String, String>> certifications = user.getCertifications() != null && !user.getCertifications().isEmpty() ?
                user.getCertifications().stream().map(cert -> {
                    Map<String, String> certMap = new HashMap<>();
                    certMap.put("name", cert.getName());
                    certMap.put("issuer", cert.getIssuer());
                    certMap.put("date", cert.getDate());
                    return certMap;
                }).collect(Collectors.toList()) :
                List.of(
                    Map.of("name", "AWS Certified Developer – Associate", "issuer", "Amazon Web Services", "date", "Jan 2024"),
                    Map.of("name", "Professional Scrum Master I", "issuer", "Scrum.org", "date", "Oct 2023")
                );
            response.put("certifications", certifications);

            // Achievements - exact format as specified
            List<Map<String, String>> achievements = user.getAchievements() != null && !user.getAchievements().isEmpty() ?
                user.getAchievements().stream().map(achievement -> {
                    Map<String, String> achievementMap = new HashMap<>();
                    achievementMap.put("name", achievement.getName());
                    achievementMap.put("description", achievement.getDescription());
                    achievementMap.put("date", achievement.getDate());
                    return achievementMap;
                }).collect(Collectors.toList()) :
                List.of(
                    Map.of("name", "Contributor to Open Source Project 'ngx-datatable'",
                           "description", "Submitted several bug fixes and feature enhancements to a popular Angular library.",
                           "date", "Mar 2024"),
                    Map.of("name", "Hackathon Winner: Best UI/UX Design",
                           "description", "Led the design and development of the front-end for a winning team at a regional hackathon.",
                           "date", "Aug 2023")
                );
            response.put("achievements", achievements);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user from token: " + e.getMessage());
        }
    }
}
