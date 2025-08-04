package com.PrepWise.services;

import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.UserRepository;
import com.PrepWise.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse registerUser(SignUpRequest request) {
        validateSignUpRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername().trim().toLowerCase());
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(savedUser);

            return new AuthResponse(token, jwtUtil.getExpirationTime());
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user: " + e.getMessage());
        }
    }

    public AuthResponse loginUser(LoginRequest request) {
        validateLoginRequest(request);

        try {
            String usernameOrEmail = request.getUsernameOrEmail().trim().toLowerCase();

            Optional<User> userOptional = userRepository.findByUsername(usernameOrEmail);

            if (userOptional.isEmpty()) {
                userOptional = userRepository.findByEmail(usernameOrEmail);
            }

            if (userOptional.isEmpty()) {
                throw new RuntimeException("Invalid username/email or password");
            }

            User user = userOptional.get();

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid username/email or password");
            }

            String token = jwtUtil.generateToken(user);
            return new AuthResponse(token, jwtUtil.getExpirationTime());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private void validateSignUpRequest(SignUpRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getUsername().trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters long");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            throw new RuntimeException("Please provide a valid email address");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request.getUsernameOrEmail() == null || request.getUsernameOrEmail().trim().isEmpty()) {
            throw new RuntimeException("Username or email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
    }
}
