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

        // 1. Bypass auth endpoints (no tenant required)
        if (uri.equals("/api/auth/register-tenant") ||
                uri.equals("/api/auth/cashier-login") ||
                uri.equals("/error")) {
            return true;
        }

        // 2. Allow preflight OPTIONS requests (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 3. Validate tenant header for all other endpoints
        String tenantId = request.getHeader(TENANT_HEADER_NAME);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            // If you have a TenantContext (ThreadLocal) – set it here
            // TenantContext.setCurrentTenant(tenantId);
            return true;
        } else {
            // Reject with 400 Bad Request
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing required X-Tenant-ID header.\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clean up tenant context to prevent memory leaks
        // TenantContext.clear();
    }
}