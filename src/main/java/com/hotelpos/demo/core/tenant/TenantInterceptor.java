package com.hotelpos.demo.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enterprise Web Filter Interceptor. Extracts tenant scoping keys from incoming
 * routing traffic headers before passing execution control to downstream REST layers.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Extract the explicit tracking token injected by the Angular interceptor pipeline
        String tenantId = request.getHeader(TENANT_HEADER_NAME);

        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // In a production SaaS setup, you could choose to block requests missing this header.
            // For early local dev testing, we pass execution context control through cleanly.
            TenantContext.setCurrentTenant("DEFAULT_TENANT_DEV");
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // ALWAYS scrub thread resources upon transactional request completion
        TenantContext.clear();
    }
}