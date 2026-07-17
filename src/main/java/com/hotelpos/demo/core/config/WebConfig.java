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
    public void addInterceptors(InterceptorRegistry registry) {
        // Enforce the tenant firewall across EVERY endpoint by default
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                // 🎯 BULLETPROOF BYPASS: Include the correct "/api" prefix so it accurately matches your AuthController!
                .excludePathPatterns("/api/auth/**", "/static/**", "/index.html", "/error");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow CORS across the entire app space for your Angular dev server
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Tenant-ID");
    }
}