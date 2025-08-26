package com.PrepWise.controllers;

import com.PrepWise.config.TestSecurityConfig;
import com.PrepWise.config.JwtAuthenticationFilter;
import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ---------------- Sign-Up Tests ----------------

    @Test
    @DisplayName("POST /api/auth/sign-up - success returns 201 with token")
    void signUp_success() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "john@example.com",
                "secret123",
                "John Doe",
                "Boston, MA",
                "https://github.com/john",
                "https://linkedin.com/in/john",
                "https://john.dev"
        );

        when(userService.registerUser(any(SignUpRequest.class)))
                .thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - missing username returns 400")
    void signUp_missingUsername() throws Exception {
        SignUpRequest req = new SignUpRequest(
                null,
                "john@example.com",
                "secret123",
                "John Doe",
                "Boston, MA",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - missing email returns 400")
    void signUp_missingEmail() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                null,
                "secret123",
                "John Doe",
                "Boston, MA",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - missing password returns 400")
    void signUp_missingPassword() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "john@example.com",
                null,
                "John Doe",
                "Boston, MA",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - service throws 'Username already exists' -> 400")
    void signUp_existingUsername_fromService() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "john@example.com",
                "secret123",
                "John Doe",
                "Boston, MA",
                null,
                null,
                null
        );

        when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    // ---------------- Sign-In Tests ----------------

    @Test
    @DisplayName("POST /api/auth/sign-in - success returns 200 with token")
    void signIn_success() throws Exception {
        LoginRequest req = new LoginRequest("john_doe", "secret123");

        when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - missing usernameOrEmail -> 401 with error")
    void signIn_missingUsernameOrEmail() throws Exception {
        LoginRequest req = new LoginRequest(null, "secret123");

        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Username or email is required"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Username or email is required"));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - missing password -> 401 with error")
    void signIn_missingPassword() throws Exception {
        LoginRequest req = new LoginRequest("john_doe", null);

        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Password is required"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - invalid credentials (user not found) -> 401")
    void signIn_invalid_userNotFound() throws Exception {
        LoginRequest req = new LoginRequest("unknown", "secret123");

        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Error logging in: Invalid credentials"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Error logging in: Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - invalid credentials (wrong password) -> 401")
    void signIn_invalid_wrongPassword() throws Exception {
        LoginRequest req = new LoginRequest("john_doe", "badpass");

        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Error logging in: Invalid credentials"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Error logging in: Invalid credentials"));
    }
}
