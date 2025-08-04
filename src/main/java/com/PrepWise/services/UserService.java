package com.PrepWise.services;

import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.UserRepository;
import com.PrepWise.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        try {
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setEmail(request.getEmail().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);

            // Generate token using string parameter
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
            if (!userOpt.isPresent()) {
                userOpt = userRepository.findByEmail(usernameOrEmail);
            }

            if (!userOpt.isPresent()) {
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
}
