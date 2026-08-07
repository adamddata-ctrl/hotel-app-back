package com.hotelpos.demo.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                // 1. Hook your customized CORS settings richard sisay
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))


                // 2. Deactivate CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // 3. Define public vs protected endpoints
                .authorizeHttpRequests(auth -> auth
                        // Allow browser preflight (OPTIONS) requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public authentication & registration endpoints
                        .requestMatchers("/api/auth/register-tenant").permitAll()
                        .requestMatchers("/api/auth/cashier-login").permitAll()

                        // Allow Spring Boot's default error handling
                        .requestMatchers("/error").permitAll()

                        // All other endpoints are validated by your TenantInterceptor
                        // (header validation + frontend guards). Keeping this permissive
                        // ensures your interceptor controls access without needing a JWT filter.
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Authorize both local dev and your live Render frontend
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://hotel-app-front-2xh0.onrender.com"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Allow all headers your frontend sends, including the custom tenant header
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Tenant-ID",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Expose the tenant ID header so the frontend can read it if needed
        configuration.setExposedHeaders(Arrays.asList("X-Tenant-ID"));

        // Allow cookies / credentials to be sent
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}