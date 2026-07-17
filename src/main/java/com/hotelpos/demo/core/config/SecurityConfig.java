package com.hotelpos.demo.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Explicitly hook your customized cross-origin settings
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Completely deactivate CSRF filter protection lines for REST engine operations
                .csrf(csrf -> csrf.disable())

                // 3. Set up explicit request routing access controls
                .authorizeHttpRequests(auth -> auth
                                // Allow browsers to pre-flight check routes without getting a 403 block!
                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Explicitly clear the path rules for your cashier-login authentication ro
                        .requestMatchers("/api/auth/cashier-login").permitAll()

                        // Allow anything else for this local development stage
                        .anyRequest().permitAll()
                )
                .build(); // Builds and returns the chain safely in one single expression thread
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Tenant-ID"));
        configuration.setExposedHeaders(List.of("X-Tenant-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}