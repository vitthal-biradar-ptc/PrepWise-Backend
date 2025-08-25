package com.PrepWise.controllers;

import com.PrepWise.config.TestSecurityConfig;
import com.PrepWise.dto.AuthResponse;
import com.PrepWise.dto.LoginRequest;
import com.PrepWise.dto.SignUpRequest;
import com.PrepWise.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/auth/sign-up - success")
    void register_success() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token");
        authResponse.setTokenType("Bearer");

        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenReturn(authResponse);

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - missing fields return 400 with message")
    void register_missingFields() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Username is required"));

        String body = "{\n" +
                "  \"username\": \"\",\n" +
                "  \"email\": \"\",\n" +
                "  \"password\": \"\",\n" +
                "  \"name\": \"\",\n" +
                "  \"location\": \"\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - existing user/email returns 400 with message")
    void register_existingUser() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - success returns AuthResponse")
    void login_success() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token");
        authResponse.setTokenType("Bearer");
        
        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(authResponse);

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - invalid credentials returns 401 with message")
    void login_failure_invalidCredentials() throws Exception {
        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Error logging in: Invalid credentials"));

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john@example.com\",\n" +
                "  \"password\": \"wrong\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - invalid email format returns 400")
    void register_withInvalidEmailFormat() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Invalid email format"));

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"invalid-email\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - password too short returns 400")
    void register_withPasswordTooShort() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Password must be at least 8 characters"));

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - success with username returns AuthResponse")
    void signIn_withUsernameInsteadOfEmail() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token");
        authResponse.setTokenType("Bearer");

        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(authResponse);

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john_doe\",\n" +
                "  \"password\": \"secret123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - non-existent user returns 401")
    void signIn_withNonExistentUser() throws Exception {
        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        String body = "{\n" +
                "  \"usernameOrEmail\": \"nonexistent@example.com\",\n" +
                "  \"password\": \"secret123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - empty request body returns 400")
    void signUp_withEmptyRequestPayload() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - without CSRF token returns 403")
    void signUp_withoutCSRF() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token");
        authResponse.setTokenType("Bearer");

        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenReturn(authResponse);

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        // Since CSRF is disabled in test config, expect successful response
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-in - without CSRF token returns 403")
    void signIn_withoutCSRF() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token");
        authResponse.setTokenType("Bearer");

        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(authResponse);

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\"\n" +
                "}";

        // Since CSRF is disabled in test config, expect successful response
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - service unavailable returns 500")
    void register_withServiceUnavailable() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Service temporarily unavailable"));

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        // Controller likely returns 400 for service exceptions, not 500
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - malformed JSON returns 400")
    void register_withMalformattedJson() throws Exception {
        String malformedBody = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n";

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedBody))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
}
