package com.hotelpos.demo.core.config;

import com.hotelpos.demo.core.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main application routing and filter configuration engine. Registers custom
 * multi-tenant interceptors and sets up global browser security protocols.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private TenantInterceptor tenantInterceptor;

    /**
     * Registers your custom Multi-Tenant Interceptor to monitor all inbound API execution tracks.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**"); // Intercepts all operational endpoints
    }

/**
 * Sets up clean Cross-Origin Resource Sharing (CORS) rules so your Angular frontend
 * application can safely communicate with this backend port without getting blocked by the browser.
 */
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200") // Default local Angular port boundary
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("X-Tenant-ID"); // Ensures browser code can read your custom tracking header
}
}