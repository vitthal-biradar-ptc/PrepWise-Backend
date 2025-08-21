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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private SignUpRequest buildValidSignUpRequest() {
        return new SignUpRequest(
                "john_doe",
                "john@example.com",
                "secret123",
                "John Doe",
                "Boston, MA",
                "https://github.com/john",
                "https://linkedin.com/in/john",
                "https://john.dev"
        );
    }

    @Test
    @DisplayName("registerUser: succeeds with valid input and returns token")
    void registerUser_success() {
        SignUpRequest request = buildValidSignUpRequest();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        User saved = new User();
        saved.setUsername("john_doe");
        saved.setEmail("john@example.com");
        saved.setPassword("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtUtil.generateToken("john_doe")).thenReturn("jwt-token");

        AuthResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("john_doe");
    }

    @Test
    @DisplayName("registerUser: fails when username already exists")
    void registerUser_existingUsername() {
        SignUpRequest request = buildValidSignUpRequest();
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(request));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: fails when email already exists")
    void registerUser_existingEmail() {
        SignUpRequest request = buildValidSignUpRequest();
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(request));
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    static Stream<Arguments> missingRequiredFieldsProvider() {
        // Each argument: request, expectedMessage
        SignUpRequest base = new SignUpRequest("john_doe", "john@example.com", "secret123", "John Doe", "Boston", null, null, null);
        SignUpRequest noUsername = new SignUpRequest(null, base.getEmail(), base.getPassword(), base.getName(), base.getLocation(), null, null, null);
        SignUpRequest blankUsername = new SignUpRequest("   ", base.getEmail(), base.getPassword(), base.getName(), base.getLocation(), null, null, null);
        SignUpRequest noEmail = new SignUpRequest(base.getUsername(), null, base.getPassword(), base.getName(), base.getLocation(), null, null, null);
        SignUpRequest blankEmail = new SignUpRequest(base.getUsername(), "   ", base.getPassword(), base.getName(), base.getLocation(), null, null, null);
        SignUpRequest noPassword = new SignUpRequest(base.getUsername(), base.getEmail(), null, base.getName(), base.getLocation(), null, null, null);
        SignUpRequest blankPassword = new SignUpRequest(base.getUsername(), base.getEmail(), "   ", base.getName(), base.getLocation(), null, null, null);
        SignUpRequest noName = new SignUpRequest(base.getUsername(), base.getEmail(), base.getPassword(), null, base.getLocation(), null, null, null);
        SignUpRequest blankName = new SignUpRequest(base.getUsername(), base.getEmail(), base.getPassword(), "   ", base.getLocation(), null, null, null);
        SignUpRequest noLocation = new SignUpRequest(base.getUsername(), base.getEmail(), base.getPassword(), base.getName(), null, null, null, null);
        SignUpRequest blankLocation = new SignUpRequest(base.getUsername(), base.getEmail(), base.getPassword(), base.getName(), "   ", null, null, null);

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
