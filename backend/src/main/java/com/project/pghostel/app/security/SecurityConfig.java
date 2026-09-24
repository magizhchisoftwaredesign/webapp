package com.project.pghostel.app.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ==========================================
    // ALLOWED ORIGINS (Netlify frontend, etc.)
    // Set via CORS_ALLOWED_ORIGINS env var on Railway,
    // comma-separated for multiple origins.
    // ==========================================

    @Value("${app.cors.allowed-origins:http://localhost:5500}")
    private String allowedOrigins;

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // ==========================================
    // CORS CONFIGURATION
    // Only needed if the frontend calls this API
    // directly (i.e. you are NOT using the Netlify
    // /api/* proxy redirect). Harmless either way.
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    // ==========================================
    // SECURITY FILTER CHAIN
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http

            // Enable CORS using the bean above
            .cors(cors -> {})

            // Disable CSRF
            .csrf(csrf -> csrf.disable())

            // JWT = Stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // ======================================
            // AUTHORIZATION
            // ======================================

            .authorizeHttpRequests(auth -> auth

                // ----------------------------------
                // FRONTEND FILES
                // ----------------------------------

                .requestMatchers(
                    "/html/**",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()


                // ----------------------------------
                // LOGIN + REGISTER
                // ----------------------------------

                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/users/reset-password"
                ).permitAll()


                // ----------------------------------
                // USER MANAGEMENT
                // ADMIN ONLY
                // ----------------------------------

                .requestMatchers(
                    "/api/users/**"
                ).hasRole("ADMIN")


                // ----------------------------------
                // OTHER API
                // ----------------------------------

                .anyRequest().authenticated()
            )


            // ======================================
            // JWT FILTER
            // ======================================

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}