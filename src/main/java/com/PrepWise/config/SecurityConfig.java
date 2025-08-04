package com.PrepWise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Allow multiple variations
                .requestMatchers(HttpMethod.POST, "/api/auth/sign-up", "/api/auth/signup", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/gemini/ask").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/gemini/analyze-resume").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/gemini/analyze-text").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/message", "/api/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight requests

                // Health check endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/error").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins in production
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}