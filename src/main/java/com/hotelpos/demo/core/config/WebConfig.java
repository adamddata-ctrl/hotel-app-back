package com.hotelpos.demo.core.config;

import com.hotelpos.demo.core.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "https://hotel-app-front-2xh0.onrender.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 🔥 CRITICAL FIX: This wires your TenantInterceptor into the Spring MVC lifecycle
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")  // Apply to all API routes
                .excludePathPatterns("/api/auth/register-tenant", "/api/auth/cashier-login", "/error"); // Safe double-check
    }
}