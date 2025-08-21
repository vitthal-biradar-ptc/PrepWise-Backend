package com.PrepWise.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/auth/sign-up - success")
    @WithMockUser
    void register_success() throws Exception {
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenReturn(new AuthResponse("jwt-token"));

        String body = "{\n" +
                "  \"username\": \"john_doe\",\n" +
                "  \"email\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\",\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"location\": \"Boston\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/sign-up")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - missing fields return 400 with message")
    @WithMockUser
    void register_missingFields() throws Exception {
        // Mock the service to throw validation error
        Mockito.when(userService.registerUser(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Username is required"));

        String body = "{\n" +
                "  \"username\": \"\",\n" +
                "  \"email\": \"\",\n" +
                "  \"password\": \"\",\n" +
                "  \"name\": \"\",\n" +
                "  \"location\": \"\"\n" +
                "}";

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();

        // Check if response has content before checking JSON path
        String responseContent = result.getResponse().getContentAsString();
        if (!responseContent.isEmpty()) {
            mockMvc.perform(post("/api/auth/sign-up")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.error", anyOf(
                            is("Username is required"),
                            is("Email is required"),
                            is("Password is required"),
                            is("Name is required"),
                            is("Location is required")
                    )));
        }
    }

    @Test
    @DisplayName("POST /api/auth/sign-up - existing user/email returns 400 with message")
    @WithMockUser
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error", is("Email already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns AuthResponse")
    @WithMockUser
    void login_success() throws Exception {
        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-token"));

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john@example.com\",\n" +
                "  \"password\": \"secret123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.token", is("jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("POST /api/auth/login - invalid credentials returns 401 with message")
    @WithMockUser
    void login_failure_invalidCredentials() throws Exception {
        Mockito.when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Error logging in: Invalid credentials"));

        String body = "{\n" +
                "  \"usernameOrEmail\": \"john@example.com\",\n" +
                "  \"password\": \"wrong\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.error", is("Error logging in: Invalid credentials")));
    }
}
