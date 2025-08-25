package com.PrepWise.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignUpRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SignUpRequest createValidSignUpRequest() {
        SignUpRequest req = new SignUpRequest();
        req.setUsername("john_doe");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setName("John Doe");
        req.setLocation("Boston");
        req.setGithubUrl("https://github.com/john");
        req.setLinkedinUrl("https://linkedin.com/in/john");
        req.setPortfolioLink("https://john.dev");
        return req;
    }

    @Test
    @DisplayName("Valid SignUpRequest passes bean validation")
    void validRequest_passes() {
        SignUpRequest req = createValidSignUpRequest();
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Invalid email format fails validation")
    void invalidEmail_fails() {
        SignUpRequest req = createValidSignUpRequest();
        req.setEmail("not-an-email");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("valid email")));
    }

    @Test
    @DisplayName("Password shorter than 6 characters fails validation")
    void weakPassword_fails() {
        SignUpRequest req = createValidSignUpRequest();
        req.setPassword("123");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("at least 6")));
    }

    @Test
    @DisplayName("Blank required fields fail validation")
    void blankFields_fail() {
        SignUpRequest req = new SignUpRequest();
        req.setUsername(" ");
        req.setEmail(" ");
        req.setPassword(" ");
        req.setName(" ");
        req.setLocation(" ");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 5);
    }
}


