package com.hotelpos.demo.core.tenant;

/**
 * Thread-scoped context storage container. Holds the active tenant identifier
 * securely for the duration of an isolated transaction processing thread execution lifecycle.
 */
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Binds an extracted tenant identifier token to the current request thread execution block.
     */
    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Resolves the bound tenant identifier for active data mapping query operations.
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Purges the thread local storage context to guarantee zero memory pollution
     * across recycled server threads.
     */
    public static void clear() {
        currentTenant.remove();
    }
}