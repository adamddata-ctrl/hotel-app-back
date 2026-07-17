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
        // Exclude preflight CORS requests automatically
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String tenantId = request.getHeader(TENANT_HEADER_NAME);

        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // Keep your local dev configuration strategy safely isolated
            TenantContext.setCurrentTenant("DEFAULT_TENANT_DEV");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 🔥 THE MISSING PIECE: Force-purges the ThreadLocal space to prevent cross-tenant security leaks
        TenantContext.clear();
    }
}