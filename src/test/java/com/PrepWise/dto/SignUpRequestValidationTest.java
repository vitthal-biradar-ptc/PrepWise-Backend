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

    @Test
    @DisplayName("Valid SignUpRequest passes bean validation")
    void validRequest_passes() {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "john@example.com",
                "secret123",
                "John Doe",
                "Boston",
                null,
                null,
                null
        );

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Invalid email format fails validation")
    void invalidEmail_fails() {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "not-an-email",
                "secret123",
                "John Doe",
                "Boston",
                null,
                null,
                null
        );

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("valid email")));
    }

    @Test
    @DisplayName("Password shorter than 6 characters fails validation")
    void weakPassword_fails() {
        SignUpRequest req = new SignUpRequest(
                "john_doe",
                "john@example.com",
                "123",
                "John Doe",
                "Boston",
                null,
                null,
                null
        );

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("at least 6")));
    }

    @Test
    @DisplayName("Blank required fields fail validation")
    void blankFields_fail() {
        SignUpRequest req = new SignUpRequest(
                " ",
                " ",
                " ",
                " ",
                " ",
                null,
                null,
                null
        );

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 5);
    }
}


