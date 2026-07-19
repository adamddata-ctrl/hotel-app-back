package com.hotelpos.demo.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 1. Explicitly bypass auth and error paths to break the internal loop completely
        if (uri.startsWith("/api/auth/") || uri.equals("/error")) {
            return true;
        }

        // Exclude preflight CORS requests automatically
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String tenantId = request.getHeader(TENANT_HEADER_NAME);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
            return true;
        } else {
            // 2. Instead of a fake string that crashes production, reject the request cleanly
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing required X-Tenant-ID header.\"}");
            return false; // Stops execution immediately before any database crash can loop
        }
    }

        @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 🔥 THE MISSING PIECE: Force-purges the ThreadLocal space to prevent cross-tenant security leaks
        TenantContext.clear();
    }
}