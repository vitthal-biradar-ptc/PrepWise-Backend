package com.PrepWise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Bean
    public String geminiApiKey() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Please set gemini.api.key in application.properties");
        }
        return geminiApiKey;
    }
}