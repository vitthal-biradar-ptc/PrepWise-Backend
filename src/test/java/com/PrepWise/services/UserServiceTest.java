package com.PrepWise.services;

import com.PrepWise.config.JwtUtil;
import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;


    private static SignUpRequest buildBaseSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("john_doe");
        request.setEmail("john@example.com");
        request.setPassword("secret123");
        request.setName("John Doe");
        request.setLocation("Boston, MA");
        request.setGithubUrl("https://github.com/john");
        request.setLinkedinUrl("https://linkedin.com/in/john");
        request.setPortfolioLink("https://john.dev");
        return request;
    }

    private static void copySignUpRequest(SignUpRequest source, SignUpRequest target) {
        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        target.setPassword(source.getPassword());
        target.setName(source.getName());
        target.setLocation(source.getLocation());
        target.setGithubUrl(source.getGithubUrl());
        target.setLinkedinUrl(source.getLinkedinUrl());
        target.setPortfolioLink(source.getPortfolioLink());
    }

    private SignUpRequest buildValidSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("john_doe");
        request.setEmail("john@example.com");
        request.setPassword("secret123");
        request.setName("John Doe");
        request.setLocation("Boston, MA");
        request.setGithubUrl("https://github.com/john");
        request.setLinkedinUrl("https://linkedin.com/in/john");
        request.setPortfolioLink("https://john.dev");
        return request;
    }

    @Test
    @DisplayName("registerUser: succeeds with valid input and returns token")
    void registerUser_success() {
        // Given
        SignUpRequest request = buildValidSignUpRequest();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword("hashed");
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = userService.registerUser(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(anyString());
    }

    @Test
    @DisplayName("registerUser: fails when username already exists")
    void registerUser_existingUsername() {
        // Given
        SignUpRequest request = buildValidSignUpRequest();
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When/Then
        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(request));
            
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: fails when email already exists")
    void registerUser_existingEmail() {
        // Given
        SignUpRequest request = buildValidSignUpRequest();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(request));
            
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    static Stream<Arguments> missingRequiredFieldsProvider() {
        // Each argument: request, expectedMessage
        SignUpRequest base = buildBaseSignUpRequest();
        
        SignUpRequest noUsername = new SignUpRequest();
        copySignUpRequest(base, noUsername);
        noUsername.setUsername(null);
        
        SignUpRequest blankUsername = new SignUpRequest();
        copySignUpRequest(base, blankUsername);
        blankUsername.setUsername("   ");
        
        SignUpRequest noEmail = new SignUpRequest();
        copySignUpRequest(base, noEmail);
        noEmail.setEmail(null);
        
        SignUpRequest blankEmail = new SignUpRequest();
        copySignUpRequest(base, blankEmail);
        blankEmail.setEmail("   ");
        
        SignUpRequest noPassword = new SignUpRequest();
        copySignUpRequest(base, noPassword);
        noPassword.setPassword(null);
        
        SignUpRequest blankPassword = new SignUpRequest();
        copySignUpRequest(base, blankPassword);
        blankPassword.setPassword("   ");
        
        SignUpRequest noName = new SignUpRequest();
        copySignUpRequest(base, noName);
        noName.setName(null);
        
        SignUpRequest blankName = new SignUpRequest();
        copySignUpRequest(base, blankName);
        blankName.setName("   ");
        
        SignUpRequest noLocation = new SignUpRequest();
        copySignUpRequest(base, noLocation);
        noLocation.setLocation(null);
        
        SignUpRequest blankLocation = new SignUpRequest();
        copySignUpRequest(base, blankLocation);
        blankLocation.setLocation("   ");

        return Stream.of(
                Arguments.of(noUsername, "Username is required"),
                Arguments.of(blankUsername, "Username is required"),
                Arguments.of(noEmail, "Email is required"),
                Arguments.of(blankEmail, "Email is required"),
                Arguments.of(noPassword, "Password is required"),
                Arguments.of(blankPassword, "Password is required"),
                Arguments.of(noName, "Name is required"),
                Arguments.of(blankName, "Name is required"),
                Arguments.of(noLocation, "Location is required"),
                Arguments.of(blankLocation, "Location is required")
        );
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFieldsProvider")
    @DisplayName("registerUser: fails when required fields are null or blank")
    void registerUser_missingFields(SignUpRequest invalid, String expectedMessage) {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(invalid));
        assertEquals(expectedMessage, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loginUser: succeeds with username and correct password")
    void loginUser_success_withUsername() {
        LoginRequest request = new LoginRequest("john_doe", "secret123");

        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("hashed");

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("john_doe")).thenReturn("jwt-token");

        AuthResponse response = userService.loginUser(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("loginUser: succeeds with email and correct password")
    void loginUser_success_withEmail() {
        LoginRequest request = new LoginRequest("john@example.com", "secret123");

        when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());

        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("john_doe")).thenReturn("jwt-token");

        AuthResponse response = userService.loginUser(request);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("loginUser: fails when user not found")
    void loginUser_invalidCredentials_userNotFound() {
        LoginRequest request = new LoginRequest("unknown", "secret123");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.loginUser(request));
        assertEquals("Error logging in: Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("loginUser: fails when password does not match")
    void loginUser_invalidCredentials_wrongPassword() {
        LoginRequest request = new LoginRequest("john_doe", "badpass");
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("hashed");

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpass", "hashed")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.loginUser(request));
        assertEquals("Error logging in: Invalid credentials", ex.getMessage());
    }
}
